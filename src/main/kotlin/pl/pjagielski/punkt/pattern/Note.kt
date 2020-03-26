package pl.pjagielski.punkt.pattern

interface Note {
    val beat: Double
    val duration: Double
    val midinote: Int
    val amp: Float
}

data class Synth(
    override val beat: Double, override val duration: Double, val name: String, override val midinote: Int,
    override val amp: Float = 1.0f
) : Note

data class MidiOut(
    override val beat: Double, override val duration: Double, val name: String, override val midinote: Int,
    override val amp: Float = 1.0f
) : Note

data class Sample(
    override val beat: Double, override val duration: Double, val name: String,
    override val amp: Float = 1.0f, override val midinote: Int = 0
) : Note

data class Loop(
    override val beat: Double, val name: String,
    val beats: Int, val startBeat: Int = 0,
    override val amp: Float = 1.0f, override val duration: Double = 0.0, override val midinote: Int = 0
) : Note
