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
    val beats: Int, val startBeat: Int = 0,
    override val amp: Float = 1.0f, override val duration: Double = 0.0, override val midinote: Int = 0
) : Note

fun Sequence<Synth>.param(param: Param, value: Number) = this.param(param.lowercase(), value)

fun Sequence<Synth>.param(param: String, value: Number) =
    this.map { it.copy(params = it.params.plus(param to value)) }

fun Sequence<Synth>.lfo(lfo: LFO, param: Param) = this.lfo(lfo, param.lowercase())

fun Sequence<Synth>.lfo(lfo: LFO, param: String) =
    this.map { it.copy(LFOs = it.LFOs.plus(lfo to param)) }
