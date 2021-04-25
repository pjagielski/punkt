package pl.pjagielski.punkt.param

import assertk.assertThat
import assertk.assertions.isCloseTo
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.Metronome
import pl.pjagielski.punkt.config.MidiConfig
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.jam.State
import pl.pjagielski.punkt.jam.Tracks
import pl.pjagielski.punkt.midi.MidiState

class LFOTest {

    val state = createState()

    @Test
    fun lfoTest() {
        val lfo = LFO(100.0, 1100.0)

        assertThat(lfo.compute(state, 0.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(state, 1.0).toDouble()).isCloseTo(1100.0, 0.1)
        assertThat(lfo.compute(state, 2.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(state, 3.0).toDouble()).isCloseTo(100.0, 0.1)
        assertThat(lfo.compute(state, 4.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(state, 5.0).toDouble()).isCloseTo(1100.0, 0.1)
        assertThat(lfo.compute(state, 6.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(state, 7.0).toDouble()).isCloseTo(100.0, 0.1)
    }

    @Test
    fun lfoLengthTest() {
        val lfo = LFO(100.0, 1100.0, length = 2.0)

        assertThat(lfo.compute(state, 0.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(state, 2.0).toDouble()).isCloseTo(1100.0, 0.1)
        assertThat(lfo.compute(state, 4.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(state, 6.0).toDouble()).isCloseTo(100.0, 0.1)
        assertThat(lfo.compute(state, 8.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(state, 10.0).toDouble()).isCloseTo(1100.0, 0.1)
    }

    @Test
    fun lfoStartTest() {
        val lfo = LFO(100.0, 1100.0, length = 2.0, startBeat = 2.0)

        assertThat(lfo.compute(state, 0.0).toDouble()).isCloseTo(100.0, 0.1)
        assertThat(lfo.compute(state, 2.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(state, 4.0).toDouble()).isCloseTo(1100.0, 0.1)
        assertThat(lfo.compute(state, 6.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(state, 8.0).toDouble()).isCloseTo(100.0, 0.1)
        assertThat(lfo.compute(state, 10.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(state, 12.0).toDouble()).isCloseTo(1100.0, 0.1)
    }

    private fun createState(): State {
        val (bpm, beats) = 120 to 8
        val metronome = Metronome(bpm, beats)
        val tracks = Tracks(emptyMap())
        val trackConfig = TrackConfig(bpm, beats, metronome, tracks)
        val midiConfig = MidiConfig(0.1, emptyList())
        val midiState = MidiState()
        return State(trackConfig, midiConfig, midiState, tracks, emptyList())
    }

}
