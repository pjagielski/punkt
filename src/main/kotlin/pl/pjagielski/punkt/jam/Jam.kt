package pl.pjagielski.punkt.jam

import com.illposed.osc.MyOSCMessage
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCMessageInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.pjagielski.punkt.Metronome
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.midiBridge
import pl.pjagielski.punkt.osc.OscServer
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

class Jam(val samples: Samples, val loops: Loops, val metronome: Metronome, val superCollider: OscServer) {

    var playing = false

    fun playAt(state: State, at: LocalDateTime) {
        val (bpm, beatsPerBar) = state.trackConfig
        val millisPerBeat = state.trackConfig.millisPerBeat

        if (!playing) return

        state.notes.forEach { note ->
            val playAt = at.plus((note.beat * millisPerBeat).toLong(), ChronoUnit.MILLIS)
            schedule(playAt.minus(150, ChronoUnit.MILLIS)) {
                playNote(note, playAt, bpm)
            }
        }

        val nextBarAt = at.plus(beatsPerBar * millisPerBeat, ChronoUnit.MILLIS)

        schedule(nextBarAt.minus(200, ChronoUnit.MILLIS)) {
            playAt(state, nextBarAt)
        }
    }

    private fun playNote(note: Note, playAt: LocalDateTime, bpm: Int) {
        when (note) {
            is Synth -> {
                val freq = midiToHz(note.midinote)
                val packet = OSCMessage(
                    "/s_new",
                    listOf(note.name, -1, 0, 1, "freq", freq, "amp", note.amp, "dur", note.duration.toFloat())
                )
                superCollider.sendInBundle(packet, playAt)
            }
            is Sample -> {
                val buffer = samples[note.name] ?: return
                val packet = OSCMessage(
                    "/s_new",
                    listOf("playSmp", -1, 0, 1, "buf", buffer.bufNum, "amp", note.amp)
                )
                superCollider.sendInBundle(packet, playAt)
            }
            is Loop -> {
                val buffer = loops[note.name] ?: return
                val packet = OSCMessage(
                    "/s_new",
                    listOf(
                        "sampler", -1, 0, 1, "buf", buffer.bufNum, "amp", note.amp, "bpm", bpm,
                        "total", buffer.beats, "beats", note.beats, "start", note.startBeat
                    )
                )
                superCollider.sendInBundle(packet, playAt)
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

    private fun schedule(time: LocalDateTime, function: () -> Unit) {
        GlobalScope.launch {
            delay(Duration.between(metronome.currentTime(), time).toMillis())
            function.invoke()
        }
    }

    fun start(state: State) {
        playing = true
        playAt(state, metronome.currentTime().plus(100, ChronoUnit.MILLIS))
    }

    fun stop() {
        playing = false
    }
}
