package pl.pjagielski.punkt.jam

import com.illposed.osc.MyOSCMessage
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCMessageInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import pl.pjagielski.punkt.Metronome
import pl.pjagielski.punkt.config.MidiConfig
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.fx.WithFX
import pl.pjagielski.punkt.osc.Group
import pl.pjagielski.punkt.osc.OscServer
import pl.pjagielski.punkt.osc.Position.HEAD
import pl.pjagielski.punkt.osc.Position.TAIL
import pl.pjagielski.punkt.param.ParamMap
import pl.pjagielski.punkt.param.Value
import pl.pjagielski.punkt.pattern.*
import pl.pjagielski.punkt.sounds.Loops
import pl.pjagielski.punkt.sounds.Samples
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.pow

data class State(
    val trackConfig: TrackConfig,
    val midiConfig: MidiConfig,
    val tracks: Tracks,
    var notes: List<Note>
)

fun midiToHz(note: Int): Float {
    return (440.0 * 2.0.pow((note - 69.0) / 12.0)).toFloat()
}

class Jam(val stateProvider: StateProvider, val samples: Samples, val loops: Loops, val metronome: Metronome,
          val superCollider: OscServer, val midiBridge: OscServer) {

    private val logger = KotlinLogging.logger {}

    var playing = false

    fun playBar(bar: Int, state: State, at: LocalDateTime) {
        val (bpm, beatsPerBar) = state.trackConfig
        val millisPerBeat = state.trackConfig.millisPerBeat

        if (!playing) return

        logger.info("bar $bar")

        state.notes = stateProvider.provide(state.trackConfig)

        metronome.bpm = bpm
        metronome.beatsPerBar = beatsPerBar

        setEffects(state, at, bar, metronome)
        playNotes(state, at, bar, metronome)

        val nextBarAt = at.plus(beatsPerBar * millisPerBeat, ChronoUnit.MILLIS)

        schedule(nextBarAt.minus(200, ChronoUnit.MILLIS)) {
            playBar(bar + 1, state, nextBarAt)
        }
    }

    private fun setEffects(state: State, at: LocalDateTime, bar: Int, metronome: Metronome) {
        val beats = state.trackConfig.beatsPerBar.toDouble()
        (0.0..beats).step(1.0).forEach { beat -> setEffectsForBeat(state, at, bar, beat, metronome) }
    }

    private fun setEffectsForBeat(state: State, at: LocalDateTime, bar: Int, beat: Double, metronome: Metronome) {
        val millisPerBeat = state.trackConfig.millisPerBeat
        val currentBeat = metronome.currentBeat(bar, beat)
        val setAt = at.plus((beat * millisPerBeat).toLong(), ChronoUnit.MILLIS)
        schedule(setAt.minus(150, ChronoUnit.MILLIS)) {
            state.tracks.asList().forEach { track ->
                track.globalFXs.asList().forEach { globalFx ->
                    val args = globalFx.params.compute(currentBeat).flatMap { it.toList() }
                    if (args.isNotEmpty()) {
                        logger.info("beat $currentBeat, fx ${globalFx.type.name}, params $args")
                    }
                    val commonArgs = listOf("bpm", metronome.bpm)
                    val ctrlPkt = OSCMessage("/n_set", listOf(globalFx.nodeId) + commonArgs + args)
                    superCollider.sendInBundle(ctrlPkt, runAt = setAt)
                }
            }
        }
    }

    private fun playNotes(state: State, at: LocalDateTime, bar: Int, metronome: Metronome) {
        val millisPerBeat = state.trackConfig.millisPerBeat
        state.notes.forEach { note ->
            val playAt = at.plus((note.beat * millisPerBeat).toLong(), ChronoUnit.MILLIS)
            schedule(playAt.minus(150, ChronoUnit.MILLIS)) {
                playNote(bar, note, playAt, metronome, state)
            }
        }
    }

    private fun ParamMap.compute(currentBeat: Double) : List<Pair<String, Float>> {
        return this.map { (k, v) ->
            val compValue = when (v) {
                is Value.Fixed -> v.value.toFloat()
                is Value.Dynamic -> v.comp.compute(currentBeat).toFloat()
            }
            k to compValue
        }
    }

    private fun playNote(bar: Int, note: Note, playAt: LocalDateTime, metronome: Metronome, state: State) {
        val currentBeat = metronome.currentBeat(bar, note.beat)
        val (midiNudge) = state.midiConfig

        val trackIdx = note.track ?: 0
        val track = state.tracks[trackIdx]

        when (note) {
            is Synth -> {
                val freq = midiToHz(note.midinote)
                val dur = note.duration.toFloat()
                val params = listOf("freq", freq, "amp", note.amp, "dur", dur)
                val synthParams = note.params.compute(currentBeat).flatMap { it.toList() }
                logger.info("beat $currentBeat, synth ${note.name}, note ${note.midinote}, params $synthParams")

                sendInGroup(note, track.bus, dur, currentBeat, playAt) {
                    node(note.name, position = HEAD, params = params + synthParams)
                }
            }
            is Sample -> {
                val buffer = samples[note.name] ?: return
                val player = "play${buffer.channels}"

                logger.info("beat $currentBeat, sample ${note.name}")

                sendInGroup(note, track.bus, buffer.length, currentBeat, playAt) {
                    node(player, position = HEAD, params = listOf("buf", buffer.bufNum, "amp", note.amp))
                }
            }
            is Loop -> {
                val buffer = loops[note.name] ?: return
                logger.info("beat $currentBeat, loop ${note.name}, length ${buffer.length}")

                sendInGroup(note, track.bus, buffer.length, currentBeat, playAt) {
                    node("sampler", position = HEAD, params = listOf(
                        "buf", buffer.bufNum, "amp", note.amp, "bpm", metronome.bpm,
                        "total", buffer.beats, "beats", note.beats, "start", note.startBeat)
                    )
                }
            }
            is MidiOut -> {
                val midiVel = 100 //  TODO
                val noteOnPacket = MyOSCMessage(
                    "/midi/note",
                    listOf(note.channel, note.midinote, midiVel, note.duration, midiNudge),
                    OSCMessageInfo("iiidd")
                )
                midiBridge.sendInBundle(listOf(noteOnPacket), runAt = playAt)
            }
        }
    }

    private fun <T : WithFX<T>> sendInGroup(item: WithFX<T>, outBus: Int?, dur: Float, currentBeat: Double, playAt: LocalDateTime, builder: Group.() -> Unit) {
        val packets = superCollider.group {
            builder()
            item.fxs.forEach { (fxName, fx) ->
                val args = fx.params.compute(currentBeat).flatMap { it.toList() }
                logger.info("beat $currentBeat, fx $fxName, params $args")
                node(fxName, position = TAIL, params = args)
            }
            val busParams = outBus?.let { listOf("outBus", outBus) } ?: emptyList()
            val params = listOf("sus", dur) + busParams
            node("freeGroup", position = TAIL, params = params)
        }

        superCollider.sendInBundle(packets, runAt = playAt)
    }

    private fun schedule(time: LocalDateTime, function: () -> Unit) {
        GlobalScope.launch {
            delay(Duration.between(metronome.currentTime(), time).toMillis())
            function.invoke()
        }
    }

    fun start(state: State) {
        playing = true

        playBar(0, state, metronome.currentTime().plus(250, ChronoUnit.MILLIS))
    }

    fun stop() {
        playing = false
    }
}
