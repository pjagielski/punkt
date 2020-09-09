package pl.pjagielski.punkt.param

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.extracting
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.melody.C
import pl.pjagielski.punkt.melody.Intervals
import pl.pjagielski.punkt.melody.Scale
import pl.pjagielski.punkt.melody.degrees
import pl.pjagielski.punkt.pattern.*

class ParamsTest {

    @Test
    fun shouldMergeParams() {
        val lfo = LFO(1, 2, 4)
        val patterns = Scale(C, Intervals.major)
            .phrase(degrees(cycle(0, 1, 2)), cycle(1.0))
            .synth("test")
            .params("a" to 1, "b" to 2)
            .params("c" to lfo)
            .beats(2)

        assertThat(patterns)
            .extracting(Synth::params)
            .containsOnly(ParamMap("a" to Value.Fixed(1), "b" to Value.Fixed(2), "c" to Value.Dynamic(lfo)))
    }

}
