package pl.pjagielski.punkt.config

data class MidiConfig(
    var nudge: Double = 0.0,
    val inputs: List<String>
)
