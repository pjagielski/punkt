package pl.pjagielski.punkt.melody

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.melody.Intervals.major
import pl.pjagielski.punkt.melody.Intervals.minor
import pl.pjagielski.punkt.pattern.Note
import pl.pjagielski.punkt.pattern.Step
import pl.pjagielski.punkt.pattern.cycle
import pl.pjagielski.punkt.pattern.synth

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
    fun shouldResolveChords() {
        val scaleC = Scale(C, major)
        val scaleD = Scale(D, minor)
        val scaleG = Scale(G, major)

        assertThat(phrase(scaleC, Chord.I)).extracting(Step::midinote)
            .containsExactly(C, E, G)

        assertThat(phrase(scaleC, Chord.II)).extracting(Step::midinote)
            .containsExactly(D, F, A)

        assertThat(phrase(scaleC, Chord.V))
            .extracting(Step::midinote)
            .containsExactly(G, B.low(), D)

        assertThat(phrase(scaleD, Chord.I))
            .extracting(Step::midinote)
            .containsExactly(D, F, A)

        assertThat(phrase(scaleG, Chord.I))
            .extracting(Step::midinote)
            .containsExactly(G, B, D.high())
    }

    private fun phrase(scale: Scale, chord: Chord) =
        scale.phrase(chords(listOf(chord)), listOf(1.0)).toList()

    @Test
    fun shouldCreateDegreePhrase() {
        val phrase = Scale(C, major)
            .phrase(degrees(listOf(0, 2, 4, 2, 0)), cycle(0.5, 1.0))
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
        val prog = listOf(Chord.I, Chord.IV)
        val phrase = Scale(C, major)
            .phrase(chords(prog), cycle(0.5, 1.0)).synth("test")
            .toList()

        assertThat(phrase)
            .extracting(Note::beat, Note::midinote, Note::duration)
            .containsExactly(
                Triple(0.0, 60, 0.5),
                Triple(0.0, 64, 0.5),
                Triple(0.0, 67, 0.5),
                Triple(0.5, 65, 1.0),
                Triple(0.5, 69, 1.0),
                Triple(0.5, 60, 1.0)
            )
    }
}

