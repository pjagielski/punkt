package pl.pjagielski.punkt

import com.illposed.osc.MyOSCMessage
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCMessageInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class State(
    var notes: List<Note>
)

class Jam(val samples: Samples, val metronome: Metronome) {

    var playing = false

    private val bpm = 100
    private val beatsPerBar = 8

    private val millisPerBeat: Long
            get() = ((60.0 / bpm) * 1000).toLong()

    fun playAt(state: State, at: LocalDateTime) {
        if (!playing) return

        state.notes.forEach { note ->
            val playAt = at.plus((note.beat * millisPerBeat).toLong(), ChronoUnit.MILLIS)
            schedule(playAt.minus(150, ChronoUnit.MILLIS)) {
                playNote(note, playAt)
            }
        }

        val nextBarAt = at.plus(beatsPerBar * millisPerBeat, ChronoUnit.MILLIS)

        schedule(nextBarAt.minus(200, ChronoUnit.MILLIS)) {
            playAt(state, nextBarAt)
        }
    }

    private fun playNote(note: Note, playAt: LocalDateTime) {
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
                val buffer = samples.buffers[note.name] ?: return
                val packet = OSCMessage(
                    "/s_new",
                    listOf("playSmp", -1, 0, 1, "buf", buffer.bufNum, "amp", note.amp)
                )
                superCollider.sendInBundle(packet, playAt)
            }
            is MidiOut -> {
                val noteOnPacket = MyOSCMessage(
                    "/midi/on",
                    listOf(16, note.midinote, 70),
                    OSCMessageInfo("iii")
                )
                schedule(
                    LocalDateTime.now()
                        .plus(200, ChronoUnit.MILLIS)) {
                    midiBridge.sendNow(noteOnPacket)
                }
                val durationMs = (note.duration * 1000).toLong()
                schedule(
                    LocalDateTime.now().plus(durationMs + 200, ChronoUnit.MILLIS)) {
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
