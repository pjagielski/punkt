package pl.pjagielski.punkt.config

data class TrackConfig(
    var bpm: Int,
    var beatsPerBar: Int
) {
    val millisPerBeat: Long
        get() = ((60.0 / bpm) * 1000).toLong()
}
