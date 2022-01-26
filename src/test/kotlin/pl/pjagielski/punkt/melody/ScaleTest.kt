package pl.pjagielski.punkt.melody

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.melody.Intervals.major
import pl.pjagielski.punkt.melody.Intervals.minor
import pl.pjagielski.punkt.pattern.*

class ScaleTest {

    @Test
    fun shouldComputeMajorScale() {
        val scale = Scale(C, major)

        assertThat(scale.note(0)).isEqualTo(C)
        assertThat(scale.note(1)).isEqualTo(D)
        assertThat(scale.note(2)).isEqualTo(E)
        assertThat(scale.note(3)).isEqualTo(F)
        assertThat(scale.note(-1)).isEqualTo(B.low())
        assertThat(scale.note(-2)).isEqualTo(A.low())
        assertThat(scale.note(-3)).isEqualTo(G.low())
    }

    @Test
    fun shouldComputeMinorScale() {
        val scale = Scale(C.sharp(), minor)

        assertThat(scale.note(0)).isEqualTo(C.sharp())
        assertThat(scale.note(1)).isEqualTo(D.sharp())
        assertThat(scale.note(2)).isEqualTo(E)
        assertThat(scale.note(3)).isEqualTo(F.sharp())
        assertThat(scale.note(-1)).isEqualTo(B - 12)
        assertThat(scale.note(-2)).isEqualTo(A - 12)
        assertThat(scale.note(-3)).isEqualTo(G.sharp() - 12)
        assertThat(scale.note(-4)).isEqualTo(F.sharp() - 12)
        assertThat(scale.note(-5)).isEqualTo(E - 12)
    }

    @Test
    fun shouldCreateDegreePhrase() {
        val phrase = Scale(C, major)
            .phrase(degrees(listOf(0, 2, 4, 2, 0)), cycle(0.5, 1.0))
            .synth("test")
            .toList()

        assertThat(phrase)
            .extracting(Note::beat, Note::duration) { it.computeForBar(0) }
            .containsExactly(
                Triple(0.0, 0.5, 60),
                Triple(0.5, 1.0, 64),
                Triple(1.5, 0.5, 67),
                Triple(2.0, 1.0, 64),
                Triple(3.0, 0.5, 60)
            )
    }

}

