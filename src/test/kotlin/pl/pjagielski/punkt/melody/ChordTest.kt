package pl.pjagielski.punkt.melody

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.melody.Intervals.major
import pl.pjagielski.punkt.melody.Intervals.minor

class ChordTest {

    @Test
    fun shouldCreateChords() {
        val Fmin = Scale(F, minor)
        val Abmaj = Scale(A.flat(), major)
        val Bbmin = Scale(B.flat(), minor)
        val Cmin = Scale(C, minor)

        assertThat(Fmin.chord(Chord.III)).isEqualTo(Abmaj.chord(Chord.I))
        assertThat(Fmin.chord(Chord.IV)).isEqualTo(Bbmin.chord(Chord.I))
        assertThat(Fmin.chord(Chord.V)).isEqualTo(Cmin.chord(Chord.I.high()))
    }
}
