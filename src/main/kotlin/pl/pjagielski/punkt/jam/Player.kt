package pl.pjagielski.punkt.jam

import com.illposed.osc.MyOSCMessage
import com.illposed.osc.OSCMessageInfo
import mu.KotlinLogging
import pl.pjagielski.punkt.Metronome
import pl.pjagielski.punkt.fx.WithFX
import pl.pjagielski.punkt.osc.Group
import pl.pjagielski.punkt.osc.OscServer
import pl.pjagielski.punkt.osc.Position
import pl.pjagielski.punkt.pattern.*
import pl.pjagielski.punkt.sounds.Loops
import pl.pjagielski.punkt.sounds.Samples
import java.time.LocalDateTime
import kotlin.math.roundToInt

class Player(val samples: Samples, val loops: Loops, val state: State,
             val metronome: Metronome, val superCollider: OscServer, val midiBridge: OscServer) {

    private val logger = KotlinLogging.logger {}

    fun playNote(bar: Int, note: Note, playAt: LocalDateTime, state: State) {
        val currentBeat = metronome.currentBeat(bar, note.beat)
        val (midiNudge) = state.midiConfig

        val trackIdx = note.track ?: 0
        val track = state.tracks[trackIdx]

        when (note) {
            is Synth -> {
                val midinote = note.midinote.compute(bar)
                val freq = midiToHz(midinote)
                val dur = note.duration.toFloat()
                val params = listOf("freq", freq, "amp", note.amp, "dur", dur)
                val synthParams = note.params.compute(state, currentBeat).flatMap { it.toList() }
                logger.info("beat $currentBeat, synth ${note.name}, note $midinote, params $synthParams")

                sendInGroup(note, track.bus, dur, currentBeat, playAt) {
                    node(note.name, position = Position.HEAD, params = params + synthParams)
                }
            }
            is Sample -> {
                val buffer = samples[note.name] ?: return
                val player = "play${buffer.channels}"

                logger.debug("beat $currentBeat, sample ${note.name}")

                sendInGroup(note, track.bus, buffer.length, currentBeat, playAt) {
                    node(player, position = Position.HEAD, params = listOf("buf", buffer.bufNum, "amp", note.amp))
                }
            }
            is Loop -> {
                val buffer = loops[note.name] ?: return
                logger.debug("beat $currentBeat, loop ${note.name}, length ${buffer.length}")

                sendInGroup(note, track.bus, buffer.length, currentBeat, playAt) {
                    node("sampler", position = Position.HEAD, params = listOf(
                        "buf", buffer.bufNum, "amp", note.amp, "bpm", metronome.bpm,
                        "total", buffer.beats, "beats", note.beats, "start", note.startBeat)
                    )
                }
            }
            is MidiOut -> {
                val midinote = note.midinote.compute(bar)
                val amp = note.amp
                val midiVel = (127 * amp).roundToInt()
                val noteOnPacket = MyOSCMessage(
                    "/midi/note",
                    listOf(note.channel, midinote, midiVel, note.duration, midiNudge),
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
                val args = fx.params.compute(state, currentBeat).flatMap { it.toList() }
                logger.debug("beat $currentBeat, fx $fxName, params $args")
                node(fxName, position = Position.TAIL, params = args)
            }
            val busParams = outBus?.let { listOf("outBus", outBus) } ?: emptyList()
            val params = listOf("sus", dur) + busParams
            node("freeGroup", position = Position.TAIL, params = params)
        }

        superCollider.sendInBundle(packets, runAt = playAt)
    }

}
