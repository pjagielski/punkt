package pl.pjagielski.punkt.pattern

import pl.pjagielski.punkt.jam.LFO

interface Note {
    val beat: Double
    val duration: Double
    val midinote: Int
    val amp: Float
}

enum class Param {
    CUTOFF, RELEASE;

    fun lowercase() = this.name.toLowerCase()
}

data class Synth(
    override val beat: Double, override val duration: Double, val name: String, override val midinote: Int,
    override val amp: Float = 1.0f,
    val params: Map<String, Number> = emptyMap(), val LFOs: Map<LFO, String> = emptyMap()
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
    val beats: Float, val startBeat: Float = 0.0f,
    override val amp: Float = 1.0f, override val duration: Double = 0.0, override val midinote: Int = 0
) : Note {
    constructor(beat: Double, name: String, beats: Int, startBeat: Double = 0.0, amp: Float = 1.0f)
            : this(beat, name, beats.toFloat(), startBeat.toFloat(), amp)
}



fun Sequence<Synth>.namedParams(vararg params: Pair<Param, Number>) = this.params(params.map { (param, value) -> param.lowercase() to value })

fun Sequence<Synth>.params(vararg params: Pair<String, Number>) = this.params(params.toList())

fun Sequence<Synth>.params(params: List<Pair<String, Number>>) =
    this.map { it.copy(params = it.params + params.toMap()) }

fun Sequence<Synth>.lfo(lfo: LFO, param: Param) = this.lfo(lfo, param.lowercase())

fun Sequence<Synth>.lfo(lfo: LFO, param: String) =
    this.map { it.copy(LFOs = it.LFOs.plus(lfo to param)) }
