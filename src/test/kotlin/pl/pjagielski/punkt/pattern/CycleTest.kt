package pl.pjagielski.punkt.pattern

import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.jupiter.api.Test

class CycleTest {

    @Test
    fun shouldCreateCycle() {
        assertThat(cycle(1..2).take(6).toList())
            .containsExactly(1, 2, 1, 2, 1, 2)
        assertThat(cycle(cycle((0..3)).take(6).toList()).take(10).toList())
            .containsExactly(0,1,2,3,0,1,0,1,2,3)
    }

    @Test
    fun shouldCreateCycleWithNulls() {
        assertThat(cycle(listOf(1, null)).flatten().take(6).toList())
            .containsExactly(1, null, 1, null, 1, null)
    }
}
