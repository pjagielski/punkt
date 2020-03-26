package pl.pjagielski.punkt.pattern

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import org.junit.jupiter.api.Test

class PatternBuilderTest {

    @Test
    fun shouldCreateUsingBuilder() {
        val pats = patterns(beats = 4) {
            +repeat(1.0).sample("bd_haus")
        }

        assertThat(pats).extracting { it.beat to it.duration }
            .containsExactly(0.0 to 1.0, 1.0 to 1.0, 2.0 to 1.0, 3.0 to 1.0)
    }
}


