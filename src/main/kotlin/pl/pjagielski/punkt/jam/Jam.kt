package pl.pjagielski.punkt.jam

import com.illposed.osc.MyOSCMessage
import com.illposed.osc.OSCMessageInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import pl.pjagielski.punkt.Clock
import pl.pjagielski.punkt.Metronome
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.midiBridge
import pl.pjagielski.punkt.osc.Group
import pl.pjagielski.punkt.osc.OscServer
import pl.pjagielski.punkt.osc.Position.HEAD
import pl.pjagielski.punkt.osc.Position.TAIL
import pl.pjagielski.punkt.pattern.*
import pl.pjagielski.punkt.sounds.Loops
import pl.pjagielski.punkt.sounds.Samples
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.pow

data class State(
    var trackConfig: TrackConfig,
    var notes: List<Note>
)

fun midiToHz(note: Int): Float {
    return (440.0 * 2.0.pow((note - 69.0) / 12.0)).toFloat()
}

class Jam(val samples: Samples, val loops: Loops, val clock: Clock, val superCollider: OscServer) {

    private val logger = KotlinLogging.logger {}

    var playing = false

    fun playAt(bar: Int, state: State, at: LocalDateTime) {
        val (bpm, beatsPerBar) = state.trackConfig
        val millisPerBeat = state.trackConfig.millisPerBeat

        if (!playing) return

        val metronome = Metronome(clock, bpm, beatsPerBar)

        logger.info("bar $bar")

        state.notes.forEach { note ->
            val playAt = at.plus((note.beat * millisPerBeat).toLong(), ChronoUnit.MILLIS)
            schedule(playAt.minus(150, ChronoUnit.MILLIS)) {
                playNote(bar, note, playAt, metronome)
            }
        }

        val nextBarAt = at.plus(beatsPerBar * millisPerBeat, ChronoUnit.MILLIS)

        schedule(nextBarAt.minus(200, ChronoUnit.MILLIS)) {
            playAt(bar + 1, state, nextBarAt)
        }
    }

    private fun ParamMap.compute(currentBeat: Double) : List<Pair<String, Float>> {
        return this.map { (k, v) ->
            val compValue = when (v) {
                is Value.Fixed -> v.value.toFloat()
                is Value.Dynamic -> v.lfo.value(currentBeat).toFloat()
            }
            k to compValue
        }
    }

    private fun playNote(bar: Int, note: Note, playAt: LocalDateTime, metronome: Metronome) {
        val currentBeat = metronome.currentBeat(bar, note.beat)

        when (note) {
            is Synth -> {
                val freq = midiToHz(note.midinote)
                val dur = note.duration.toFloat()
                val params = listOf("freq", freq, "amp", note.amp, "dur", dur)
                val synthParams = note.params.compute(currentBeat).flatMap { it.toList() }
                logger.info("beat $currentBeat, synth ${note.name}, params $synthParams")

                sendInGroup(note, dur, currentBeat, playAt) {
                    node(note.name, position = HEAD, params = params + synthParams)
                }
            }
            is Sample -> {
                val buffer = samples[note.name] ?: return
                val player = "play${buffer.channels}"

                logger.info("beat $currentBeat, sample ${note.name}")

                sendInGroup(note, buffer.length, currentBeat, playAt) {
                    node(player, position = HEAD, params = listOf("buf", buffer.bufNum, "amp", note.amp))
                }
            }
            is Loop -> {
                val buffer = loops[note.name] ?: return

                logger.info("beat $currentBeat, loop ${note.name}, length ${buffer.length}")

                sendInGroup(note, buffer.length, currentBeat, playAt) {
                    node("sampler", position = HEAD, params = listOf(
                        "buf", buffer.bufNum, "amp", note.amp, "bpm", metronome.bpm,
                        "total", buffer.beats, "beats", note.beats, "start", note.startBeat)
                    )
                }
            }
            is MidiOut -> {
                val noteOnPacket = MyOSCMessage(
                    "/midi/on",
                    listOf(16, note.midinote, 70),
                    OSCMessageInfo("iii")
                )
                schedule(LocalDateTime.now().plus(200, ChronoUnit.MILLIS)) {
                    midiBridge.sendNow(noteOnPacket)
                }
                val durationMs = (note.duration * 1000).toLong()
                schedule(LocalDateTime.now().plus(durationMs + 200, ChronoUnit.MILLIS)) {
                    val noteOffPacket = MyOSCMessage(
                        "/midi/off",
                        listOf(16, note.midinote, 70),
                        OSCMessageInfo("iii")
                    )
                    midiBridge.sendNow(noteOffPacket)
                }
            }
        }
    }

    private fun <T : WithFX<T>> sendInGroup(item: WithFX<T>, dur: Float, currentBeat: Double, playAt: LocalDateTime, builder: Group.() -> Unit) {
        val packets = superCollider.group {
            builder()
            item.fxs.forEach { fxName, fx ->
                val args = fx.params.compute(currentBeat).flatMap { it.toList() }
                logger.info("beat $currentBeat, fx $fxName, params $args")
                node(fxName, position = TAIL, params = args)
            }
            node("freeGroup", position = TAIL, params = listOf("sus", dur))
        }

        superCollider.sendInBundle(packets, runAt = playAt)
    }

    private fun schedule(time: LocalDateTime, function: () -> Unit) {
        GlobalScope.launch {
            delay(Duration.between(clock.currentTime(), time).toMillis())
            function.invoke()
        }
    }

    fun start(state: State) {
        playing = true
        playAt(0, state, clock.currentTime().plus(250, ChronoUnit.MILLIS))
    }

    fun stop() {
        playing = false
    }
}
