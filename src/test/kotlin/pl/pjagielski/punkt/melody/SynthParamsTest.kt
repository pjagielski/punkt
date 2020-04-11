package pl.pjagielski.punkt.melody

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.extracting
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.melody.Intervals.minor
import pl.pjagielski.punkt.pattern.params
import pl.pjagielski.punkt.pattern.repeat
import pl.pjagielski.punkt.pattern.synth

class SynthParamsTest {

    @Test
    fun shouldPassSynthParams() {
        val p1 = Scale(C, minor).phrase(degrees(listOf(0,1,2)), repeat(1.0))
            .synth("test").params("cutoff" to 2000).toList()

        assertThat(p1).extracting { it.params["cutoff"] }
            .containsOnly(2000)
    }
}
