package pl.pjagielski.punkt

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.Intervals.major

class ScaleTest {

    @Test
    fun shouldComputeNote() {
        val scale = Scale(C, major)

        assertThat(scale.note(0)).isEqualTo(C)
        assertThat(scale.note(1)).isEqualTo(D)
        assertThat(scale.note(2)).isEqualTo(E)
        assertThat(scale.note(3)).isEqualTo(F)
        assertThat(scale.note(-1)).isEqualTo(B - 12)
        assertThat(scale.note(-2)).isEqualTo(A - 12)
        assertThat(scale.note(-3)).isEqualTo(G - 12)
    }

    @Test
    fun shouldCreateDegreePhrase() {
        val phrase = Scale(C, major).phrase(degrees(0, 2, 4, 2, 0), cycle(0.5, 1.0))
            .synth("test")
            .toList()

        assertThat(phrase)
            .extracting(Note::beat, Note::midinote, Note::duration)
            .containsExactly(
                Triple(0.0, 60, 0.5),
                Triple(0.5, 64, 1.0),
                Triple(1.5, 67, 0.5),
                Triple(2.0, 64, 1.0),
                Triple(3.0, 60, 0.5)
            )
    }

    @Test
    fun shouldCreateChordPhrase() {
        val phrase = Scale(C, major)
            .phrase(chords(listOf(0, 2), listOf(4, 2)), cycle(0.5, 1.0)).synth("test")
            .toList()

        assertThat(phrase)
            .extracting(Note::beat, Note::midinote, Note::duration)
            .containsAll(
                Triple(0.0, 60, 0.5),
                Triple(0.0, 64, 0.5),
                Triple(0.5, 64, 1.0),
                Triple(0.5, 67, 1.0)
            )
    }
}

