package pl.pjagielski.punkt.pattern

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import org.junit.jupiter.api.Test

class PatternTest {

    @Test
    fun shouldCreatePattern() {
        val p1 = repeat(1.0).sample("bd_haus").beats(4)
        val p2 = cycle(1.0, 0.5).sample("bd_haus").beats(4)
        val p3 = cycle(0.75, 1.25).sample("bd_haus").beats(8)
        val p4 = cycle(2).sample("claps", at = 1.0).beats(4)

        assertThat(p1).extracting { it.beat to it.duration }
            .containsExactly(0.0 to 1.0, 1.0 to 1.0, 2.0 to 1.0, 3.0 to 1.0)
        assertThat(p2).extracting { it.beat to it.duration }
            .containsExactly(0.0 to 1.0, 1.0 to 0.5, 1.5 to 1.0, 2.5 to 0.5, 3.0 to 1.0)
        assertThat(p3).extracting { it.beat to it.duration }
            .containsExactly(0.0 to 0.75, 0.75 to 1.25, 2.0 to 0.75, 2.75 to 1.25, 4.0 to 0.75, 4.75 to 1.25, 6.0 to 0.75, 6.75 to 1.25)
        assertThat(p4).extracting { it.beat to it.duration }
            .containsExactly(1.0 to 2.0, 3.0 to 2.0)
    }
}
