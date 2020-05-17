package pl.pjagielski.punkt.note

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.extracting
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.pattern.*

class FXTest {

    @Test
    fun shouldMergeFXParams() {
        val patterns = repeat(1.0).sample("test")
            .fx("test-fx", "a" to 1)
            .fx("test-fx", "b" to 2)
            .beats(2)

        assertThat(patterns)
            .extracting(Sample::fxs)
            .containsOnly(FXMap("test-fx" to FX("test-fx", "a" to Value.Fixed(1), "b" to Value.Fixed(2))))
    }

    @Test
    fun shouldCreateValueList() {
        val patterns = repeat(1.0).sample("test")
            .fx("test-fx", "a" to cycle(1, 2, 3))
            .beats(4)

        assertThat(patterns)
            .extracting(Sample::fxs)
            .containsExactly(
                FXMap("test-fx" to FX("test-fx", "a" to Value.Fixed(1))),
                FXMap("test-fx" to FX("test-fx", "a" to Value.Fixed(2))),
                FXMap("test-fx" to FX("test-fx", "a" to Value.Fixed(3))),
                FXMap("test-fx" to FX("test-fx", "a" to Value.Fixed(1)))
            )
    }
}
