package pl.pjagielski.punkt.melody

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.extracting
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.melody.Intervals.minor
import pl.pjagielski.punkt.pattern.Param.CUTOFF
import pl.pjagielski.punkt.pattern.param
import pl.pjagielski.punkt.pattern.repeat
import pl.pjagielski.punkt.pattern.synth

class SynthParamsTest {

    @Test
    fun shouldPassSynthParams() {
        val p1 = Scale(C, minor).phrase(degrees(listOf(0,1,2)), repeat(1.0))
            .synth("test").param(CUTOFF, 2000).toList()

        assertThat(p1).extracting { it.params[CUTOFF.lowercase()] }
            .containsOnly(2000)
    }
}
