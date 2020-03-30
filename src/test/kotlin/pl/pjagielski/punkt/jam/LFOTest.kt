package pl.pjagielski.punkt.jam

import assertk.assertThat
import assertk.assertions.isCloseTo
import org.junit.jupiter.api.Test

class LFOTest {

    @Test
    fun lfoTest() {
        val lfo = LFO(100.0, 1100.0)

        assertThat(lfo.value(0.0)).isCloseTo(600.0, 0.1)
        assertThat(lfo.value(1.0)).isCloseTo(1100.0, 0.1)
        assertThat(lfo.value(2.0)).isCloseTo(600.0, 0.1)
        assertThat(lfo.value(3.0)).isCloseTo(100.0, 0.1)
        assertThat(lfo.value(4.0)).isCloseTo(600.0, 0.1)
        assertThat(lfo.value(5.0)).isCloseTo(1100.0, 0.1)
        assertThat(lfo.value(6.0)).isCloseTo(600.0, 0.1)
        assertThat(lfo.value(7.0)).isCloseTo(100.0, 0.1)
    }

    @Test
    fun lfoLenghtTest() {
        val lfo = LFO(100.0, 1100.0, length = 2.0)

        assertThat(lfo.value(0.0)).isCloseTo(600.0, 0.1)
        assertThat(lfo.value(2.0)).isCloseTo(1100.0, 0.1)
        assertThat(lfo.value(4.0)).isCloseTo(600.0, 0.1)
        assertThat(lfo.value(6.0)).isCloseTo(100.0, 0.1)
        assertThat(lfo.value(8.0)).isCloseTo(600.0, 0.1)
        assertThat(lfo.value(10.0)).isCloseTo(1100.0, 0.1)
    }

}
