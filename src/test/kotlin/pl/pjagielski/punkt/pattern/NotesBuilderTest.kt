package pl.pjagielski.punkt.pattern

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.melody.*
import pl.pjagielski.punkt.melody.Intervals.*
import pl.pjagielski.punkt.pattern.Note
import pl.pjagielski.punkt.pattern.cycle
import pl.pjagielski.punkt.pattern.patterns
import pl.pjagielski.punkt.pattern.synth

class NotesBuilderTest {

    @Test
    fun shouldCreateNotePattern() {
        val scale = Scale(
            C.sharp(),
            minor
        )
        val notes = patterns(beats = 8) {
            +scale
                .phrase(
                    degrees(
                        listOf(
                            0,
                            0,
                            0,
                            -4,
                            -4,
                            -4,
                            -2,
                            -2,
                            -2,
                            -1,
                            -1,
                            -1
                        )
                    ), cycle(0.75, 0.75, 0.5)
                )
                .synth("shape")

            val ch1 = listOf(7, 4)
            val ch2 = listOf(9, 4)
            val ch3 = listOf(7, 5)
            val ch4 = listOf(9, 5)
            val ch5 = listOf(8, 3)
            val ch6 = listOf(7, 3)
            val ch7 = listOf(6, 3)

            +scale.phrase(
                chords(
                    listOf(
                        ch1,
                        ch2,
                        ch1,
                        ch3,
                        ch4,
                        ch3,
                        ch3,
                        ch4,
                        ch3,
                        ch5,
                        ch6,
                        ch7
                    )
                ),
                cycle(0.75, 0.75, 0.5)
            ).synth("shape")
        }

        assertThat(notes)
            .extracting(Note::beat, Note::duration, Note::midinote)
            .containsExactly(
                Triple(0.0, 0.75, 61),
                Triple(0.0, 0.75, 68),
                Triple(0.0, 0.75, 73),
                Triple(0.75, 0.75, 61),
                Triple(0.75, 0.75, 68),
                Triple(0.75, 0.75, 76),
                Triple(1.5, 0.5, 61),
                Triple(1.5, 0.5, 68),
                Triple(1.5, 0.5, 73),
                Triple(2.0, 0.75, 54),
                Triple(2.0, 0.75, 69),
                Triple(2.0, 0.75, 73),
                Triple(2.75, 0.75, 54),
                Triple(2.75, 0.75, 69),
                Triple(2.75, 0.75, 76),
                Triple(3.5, 0.5, 54),
                Triple(3.5, 0.5, 69),
                Triple(3.5, 0.5, 73),
                Triple(4.0, 0.75, 57),
                Triple(4.0, 0.75, 69),
                Triple(4.0, 0.75, 73),
                Triple(4.75, 0.75, 57),
                Triple(4.75, 0.75, 69),
                Triple(4.75, 0.75, 76),
                Triple(5.5, 0.5, 57),
                Triple(5.5, 0.5, 69),
                Triple(5.5, 0.5, 73),
                Triple(6.0, 0.75, 59),
                Triple(6.0, 0.75, 66),
                Triple(6.0, 0.75, 75),
                Triple(6.75, 0.75, 59),
                Triple(6.75, 0.75, 66),
                Triple(6.75, 0.75, 73),
                Triple(7.5, 0.5, 59),
                Triple(7.5, 0.5, 66),
                Triple(7.5, 0.5, 71)
            )
    }
}
