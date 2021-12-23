package pl.pjagielski.punkt.jam

import com.illposed.osc.OSCMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import pl.pjagielski.punkt.Metronome
import pl.pjagielski.punkt.config.MidiConfig
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.midi.MidiState
import pl.pjagielski.punkt.osc.OscServer
import pl.pjagielski.punkt.pattern.Note
import pl.pjagielski.punkt.pattern.step
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.pow

data class State(
    val trackConfig: TrackConfig,
    val midiConfig: MidiConfig,
    val midiState: MidiState,
    val tracks: Tracks,
    var notes: List<Note>
)

fun midiToHz(note: Int): Float {
    return (440.0 * 2.0.pow((note - 69.0) / 12.0)).toFloat()
}

class Jam(val stateProvider: StateProvider, val metronome: Metronome, val superCollider: OscServer, val player: Player) {

    private val logger = KotlinLogging.logger {}

    var playing = false

    fun playBar(bar: Int, state: State, at: LocalDateTime) {
        val (bpm, beatsPerBar) = state.trackConfig
        val millisPerBeat = state.trackConfig.millisPerBeat
        val startedAt = metronome.startAt

        if (!playing) return

        logger.info("bar $bar")

        state.notes = stateProvider.provide(state.trackConfig)

        metronome.bpm = bpm
        metronome.beatsPerBar = beatsPerBar

        setEffects(state, at, bar)
        playNotes(state, at, bar, startedAt)

        val nextBarAt = at.plus((beatsPerBar.toDouble() * millisPerBeat.toDouble()).toLong(), ChronoUnit.MILLIS)

        schedule(nextBarAt.minus(200, ChronoUnit.MILLIS)) {
            if (playing && startedAt == metronome.startAt) {
                playBar(bar + 1, state, nextBarAt)
            }
        }
    }

    private fun setEffects(state: State, at: LocalDateTime, bar: Int) {
        val beats = state.trackConfig.beatsPerBar.toDouble()
        (0.0..beats).step(1.0).forEach { beat -> setEffectsForBeat(state, at, bar, beat) }
    }

    private fun setEffectsForBeat(state: State, at: LocalDateTime, bar: Int, beat: Double) {
        val millisPerBeat = state.trackConfig.millisPerBeat
        val currentBeat = metronome.currentBeat(bar, beat)
        val setAt = at.plus((beat * millisPerBeat).toLong(), ChronoUnit.MILLIS)
        schedule(setAt.minus(150, ChronoUnit.MILLIS)) {
            state.tracks.asList().forEach { track ->
                track.globalFXs.asList().forEach { globalFx ->
                    val args = globalFx.params.compute(state, currentBeat).flatMap { it.toList() }
                    if (args.isNotEmpty()) {
                        logger.debug("beat $currentBeat, fx ${globalFx.type.name}, params $args")
                    }
                    val commonArgs = listOf("bpm", metronome.bpm)
                    val ctrlPkt = OSCMessage("/n_set", listOf(globalFx.nodeId) + commonArgs + args)
                    superCollider.sendInBundle(ctrlPkt, runAt = setAt)
                }
            }
        }
    }

    private fun playNotes(state: State, at: LocalDateTime, bar: Int, startedAt: LocalDateTime) {
        val millisPerBeat = state.trackConfig.millisPerBeat
        state.notes.forEach { note ->
            val playAt = at.plus((note.beat * millisPerBeat).toLong(), ChronoUnit.MILLIS)
            schedule(playAt.minus(150, ChronoUnit.MILLIS)) {
                // check if not stopped or restarted
                if (playing && startedAt == metronome.startAt) {
                    player.playNote(bar, note, playAt, state)
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
        metronome.start()

        playBar(0, state, metronome.currentTime().plus(250, ChronoUnit.MILLIS))
    }

    fun stop() {
        playing = false
    }
}
