package pl.pjagielski.punkt.pattern

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.melody.*
import pl.pjagielski.punkt.melody.Intervals.*

class EveryTest {

    val scale = Scale(C, major)

    @Test
    fun shouldProcessNoteWithEvery() {
        val phrase = scale.phrase(
            degrees(repeat(0)), repeat(1.0)
        ).take(8).every(4, Step::low)

        assertThat(phrase.toList()).extracting(Step::midinote)
            .containsExactly(C.low(), C, C, C, C.low(), C, C, C)
    }

    @Test
    fun shouldProcessNoteWithEveryStartingFromGivenIndex() {
        val phrase = scale.phrase(
            degrees(repeat(0)), repeat(1.0)
        ).take(8).every(4, Step::low, from = 2)

        assertThat(phrase.toList()).extracting(Step::midinote)
            .containsExactly(C, C, C.low(), C, C, C, C.low(), C)
    }

    @Test
    fun shouldAddRests() {
        val phrase = scale.phrase(
            degrees(repeat(0)), repeat(1.0)
        ).take(8).every(3, Step::rest, from = 3)

        assertThat(phrase.toList()).extracting(Step::beat)
            .containsExactly(0.0, 1.0, 2.0, 4.0, 5.0, 7.0)
    }

    @Test
    fun shouldAddRestsToAll() {
        val phrase = scale.phrase(
            degrees(repeat(0)), repeat(1.0)
        ).take(8).all(Step::rest, from = 3)

        assertThat(phrase.toList()).extracting(Step::beat)
            .containsExactly(0.0, 1.0, 2.0)
    }
}
