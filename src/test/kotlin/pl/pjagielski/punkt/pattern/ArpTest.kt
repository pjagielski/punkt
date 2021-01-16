package pl.pjagielski.punkt.pattern

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import org.junit.jupiter.api.Test
import pl.pjagielski.punkt.melody.*
import pl.pjagielski.punkt.melody.Intervals.minor

class ArpTest {

    @Test
    fun shouldCreateUpArp() {
        val indexes = arp(0, 5, 14, ArpType.UP)

        assertThat(indexes)
            .containsExactly(0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5, 0, 1)
    }

    @Test
    fun shouldCreateUpDownArp() {
        val indexes = arp(0, 5, 14, ArpType.UPDOWN)

        assertThat(indexes)
            .containsExactly(0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1)
    }

    @Test
    fun shouldCreateDownChordArp() {
        val indexes = arp(Chord.I, 9, ArpType.DOWN)

        assertThat(indexes)
            .containsExactly(0, 4, 2, 0, 4, 2, 0, 4, 2)
    }

    @Test
    fun shouldCreateUpDownChordArp() {
        val indexes = arp(Chord.I, 9, ArpType.UPDOWN)

        assertThat(indexes)
            .containsExactly(0, 2, 4, 2, 0, 2, 4, 2, 0)
    }

    @Test
    fun shouldUseArpWithChordProgression() {
        val scale = Scale(E, minor)
        val progression = listOf(Chord.I, Chord.IV, Chord.VI, Chord.VII)

        val pats = patterns(beats = 8) {
            + scale
                .phrase(degrees(progression.flatMap { arp(it, 6) }), repeat(0.25))
                .synth("test")
        }

        assertThat(pats).extracting { it.midinote }
            .containsExactly(
                64, 67, 71, 64, 67, 71, 69, 72, 76, 69, 72, 76, 72, 76, 79, 72, 76, 79, 74, 78, 81, 74, 78, 81
            )
    }
}
