package pl.pjagielski.punkt.param

import assertk.assertThat
import assertk.assertions.isCloseTo
import org.junit.jupiter.api.Test

class LFOTest {

    @Test
    fun lfoTest() {
        val lfo = LFO(100.0, 1100.0)

        assertThat(lfo.compute(0.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(1.0).toDouble()).isCloseTo(1100.0, 0.1)
        assertThat(lfo.compute(2.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(3.0).toDouble()).isCloseTo(100.0, 0.1)
        assertThat(lfo.compute(4.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(5.0).toDouble()).isCloseTo(1100.0, 0.1)
        assertThat(lfo.compute(6.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(7.0).toDouble()).isCloseTo(100.0, 0.1)
    }

    @Test
    fun lfoLengthTest() {
        val lfo = LFO(100.0, 1100.0, length = 2.0)

        assertThat(lfo.compute(0.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(2.0).toDouble()).isCloseTo(1100.0, 0.1)
        assertThat(lfo.compute(4.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(6.0).toDouble()).isCloseTo(100.0, 0.1)
        assertThat(lfo.compute(8.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(10.0).toDouble()).isCloseTo(1100.0, 0.1)
    }

    @Test
    fun lfoStartTest() {
        val lfo = LFO(100.0, 1100.0, length = 2.0, startBeat = 2.0)

        assertThat(lfo.compute(0.0).toDouble()).isCloseTo(100.0, 0.1)
        assertThat(lfo.compute(2.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(4.0).toDouble()).isCloseTo(1100.0, 0.1)
        assertThat(lfo.compute(6.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(8.0).toDouble()).isCloseTo(100.0, 0.1)
        assertThat(lfo.compute(10.0).toDouble()).isCloseTo(600.0, 0.1)
        assertThat(lfo.compute(12.0).toDouble()).isCloseTo(1100.0, 0.1)
    }

}
