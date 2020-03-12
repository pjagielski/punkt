package pl.pjagielski.punkt

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
