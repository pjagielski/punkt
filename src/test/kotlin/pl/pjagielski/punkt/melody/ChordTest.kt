package pl.pjagielski.punkt.melody

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.melody.Intervals.major
import pl.pjagielski.punkt.melody.Intervals.minor

class ChordTest {

    @Test
    fun shouldCreateChords() {
        val Fmin = Scale(F, minor)
        val Abmaj = Scale(A.flat(), major)
        val Amin = Scale(A, minor)
        val Bbmin = Scale(B.flat(), minor)
        val Cmin = Scale(C, minor)

        val chord = Abmaj.chord(Chord.I)
        assertThat(Fmin.chord(Chord.III)).isEqualTo(chord)
        assertThat(Fmin.chord(Chord.IV)).isEqualTo(Bbmin.chord(Chord.I))
        assertThat(Fmin.chord(Chord.V)).isEqualTo(Cmin.chord(Chord.I.high()))
        assertThat(Amin.chord(Chord(4, listOf(0, 3, 4)).inversion(2).low()))
            .containsExactly(B, E.high(), A.high())
    }
}
