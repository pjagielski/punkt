package pl.pjagielski.punkt.config

import pl.pjagielski.punkt.Metronome
import pl.pjagielski.punkt.jam.Tracks

data class TrackConfig(
    var bpm: Int,
    var beatsPerBar: Int,
    val metronome: Metronome,
    val tracks: Tracks
) {
    val millisPerBeat: Long
        get() = (secsPerBeat * 1000).toLong()

    val secsPerBeat: Double
        get() = 60.0 / bpm

}
