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

sealed class Value {
    data class Fixed(val value: Number) : Value()
    data class Dynamic(val lfo: LFO) : Value()
}

data class ParamMap(
    val paramMap: Map<String, Value> = emptyMap()
) : Map<String, Value> by paramMap {
    constructor(vararg params: Pair<String, Value>) : this(params.toMap())
    fun withParams(vararg params: Pair<String, Value>) = ParamMap(paramMap + params)
}

fun emptyParamMap() = ParamMap(emptyMap())

data class FX(
    val name: String,
    val params: ParamMap = emptyParamMap()
) {
    constructor(name: String, vararg params: Pair<String, Value>) : this(name, ParamMap(*params))
    fun withParams(vararg params: Pair<String, Value>) = this.copy(params = this.params.withParams(*params))
}

data class FXMap(
    val fxMap: Map<String, FX> = emptyMap()
): Map<String, FX> by fxMap {
    constructor(vararg fxs: Pair<String, FX>) : this(fxs.toMap())
    fun withFX(name: String, vararg params: Pair<String, Value>)
            = FXMap(fxMap + (name to fxMap.getOrDefault(name, FX(name)).withParams(*params)))
}

fun emptyFXMap() = FXMap(emptyMap())

@JvmName("numArrToValues") fun Array<out Pair<String, Number>>.toValues() = this.map { (k, v) -> k to Value.Fixed(v) }.toTypedArray()
@JvmName("floArrToValues") fun Array<out Pair<String, LFO>>.toValues() = this.map { (k, v) -> k to Value.Dynamic(v) }.toTypedArray()

interface WithFX<T : WithFX<T>> {
    val fxs: FXMap

    fun addFxN(name: String, vararg params: Pair<String, Number>): T = addFx(name, *params.toValues())
    fun addFxL(name: String, vararg params: Pair<String, LFO>): T = addFx(name, *params.toValues())
    fun addFx(name: String, vararg params: Pair<String, Value>): T
}

interface WithParams<T : WithParams<T>> {
    val params: ParamMap

    fun addParamsN(vararg params: Pair<String, Number>): T = addParams(*params.toValues())
    fun addParamsL(vararg params: Pair<String, LFO>): T = addParams(*params.toValues())
    fun addParams(vararg params: Pair<String, Value>): T

}

data class Synth(
    override val beat: Double, override val duration: Double, val name: String, override val midinote: Int,
    override val amp: Float = 1.0f, override val params: ParamMap = emptyParamMap(), override val fxs: FXMap = emptyFXMap()
) : Note, WithFX<Synth>, WithParams<Synth> {

    override fun addFx(name: String, vararg params: Pair<String, Value>) = copy(fxs = fxs.withFX(name, *params))
    override fun addParams(vararg params: Pair<String, Value>) = copy(params = this.params.withParams(*params))
}

data class MidiOut(
    override val beat: Double, override val duration: Double, val name: String, override val midinote: Int,
    override val amp: Float = 1.0f
) : Note

data class Sample(
    override val beat: Double, override val duration: Double, val name: String,
    override val amp: Float = 1.0f, override val midinote: Int = 0,
    override val fxs: FXMap = emptyFXMap()
) : Note, WithFX<Sample> {

    override fun addFx(name: String, vararg params: Pair<String, Value>) = copy(fxs = fxs.withFX(name, *params))
}

data class Loop(
    override val beat: Double, val name: String,
    val beats: Float, val startBeat: Float = 0.0f,
    override val amp: Float = 1.0f, override val duration: Double = 0.0, override val midinote: Int = 0,
    override val fxs: FXMap = emptyFXMap()
) : Note, WithFX<Loop> {
    constructor(beat: Double, name: String, beats: Int, startBeat: Double = 0.0, amp: Float = 1.0f)
            : this(beat, name, beats.toFloat(), startBeat.toFloat(), amp)

    override fun addFx(name: String, vararg params: Pair<String, Value>) = copy(fxs = fxs.withFX(name, *params))
}

//fun Sequence<Synth>.namedParams(vararg params: Pair<Param, Number>) = this.params(params.map { (param, value) -> param.lowercase() to value })
@JvmName("seqSynthParamsN") fun Sequence<Synth>.params(vararg params: Pair<String, Number>) = this.map { it.addParamsN(*params) }
@JvmName("seqSynthParamsL") fun Sequence<Synth>.params(vararg params: Pair<String, LFO>) = this.map { it.addParamsL(*params) }

@JvmName("listSynthFx") fun List<Sequence<Synth>>.fx(name: String) = this.map { it.fx(name) }
@JvmName("listSynthFxN") fun List<Sequence<Synth>>.fx(name: String, vararg params: Pair<String, Number>) = this.map { it.fx(name, *params) }
@JvmName("listSynthFxL") fun List<Sequence<Synth>>.fx(name: String, vararg params: Pair<String, LFO>) = this.map { it.fx(name, *params) }

@JvmName("listSampleFx") fun List<Sequence<Sample>>.fx(name: String) = this.map { it.fx(name) }
@JvmName("listSampleFxN") fun List<Sequence<Sample>>.fx(name: String, vararg params: Pair<String, Number>) = this.map { it.fx(name, *params) }
@JvmName("listSampleFxF") fun List<Sequence<Sample>>.fx(name: String, vararg params: Pair<String, LFO>) = this.map { it.fx(name, *params) }

@JvmName("listLoopFx") fun List<Sequence<Loop>>.fx(name: String) = this.map { it.fx(name) }
@JvmName("listLoopFxN") fun List<Sequence<Loop>>.fx(name: String, vararg params: Pair<String, Number>) = this.map { it.fx(name, *params) }
@JvmName("listLoopFxL") fun List<Sequence<Loop>>.fx(name: String, vararg params: Pair<String, LFO>) = this.map { it.fx(name, *params) }

@JvmName("seqSynthFx") fun Sequence<Synth>.fx(name: String) = this.map { it.addFxN(name) }
@JvmName("seqSynthFxN") fun Sequence<Synth>.fx(name: String, vararg params: Pair<String, Number>) = this.map { it.addFxN(name, *params) }
@JvmName("seqSynthFxL") fun Sequence<Synth>.fx(name: String, vararg params: Pair<String, LFO>) = this.map { it.addFxL(name, *params) }

@JvmName("seqSampleFx") fun Sequence<Sample>.fx(name: String) = this.map { it.addFxN(name) }
@JvmName("seqSampleFxN") fun Sequence<Sample>.fx(name: String, vararg params: Pair<String, Number>) = this.map { it.addFxN(name, *params) }
@JvmName("seqSampleFxL") fun Sequence<Sample>.fx(name: String, vararg params: Pair<String, LFO>) = this.map { it.addFxL(name, *params) }

@JvmName("seqLoopFx") fun Sequence<Loop>.fx(name: String) = this.map { it.addFxN(name) }
@JvmName("seqLoopFxN") fun Sequence<Loop>.fx(name: String, vararg params: Pair<String, Number>) = this.map { it.addFxN(name, *params) }
@JvmName("seqLoopFxL") fun Sequence<Loop>.fx(name: String, vararg params: Pair<String, LFO>) = this.map { it.addFxL(name, *params) }

@JvmName("sampleFxNSeq") fun Sequence<Sample>.fx(name: String, param: Pair<String, Sequence<Number>>) =
    this.zip(param.second).map { (smp, value) -> smp.addFxN(name, param.first to value) }

@JvmName("sampleFxNList") fun Sequence<Sample>.fx(name: String, param: Pair<String, List<Number>>) =
    this.fx(name, param.first to param.second.asSequence())

@JvmName("synthFxNSeq") fun Sequence<Synth>.fx(name: String, param: Pair<String, Sequence<Number>>) =
    this.zip(param.second).map { (smp, value) -> smp.addFxN(name, param.first to value) }

@JvmName("synthFxNList") fun Sequence<Synth>.fx(name: String, param: Pair<String, List<Number>>) =
    this.fx(name, param.first to param.second.asSequence())

@JvmName("loopFxNSeq") fun Sequence<Loop>.fx(name: String, param: Pair<String, Sequence<Number>>) =
    this.zip(param.second).map { (smp, value) -> smp.addFxN(name, param.first to value) }

@JvmName("loopFxNList") fun Sequence<Loop>.fx(name: String, param: Pair<String, List<Number>>) =
    this.fx(name, param.first to param.second.asSequence())

fun <T : Note> Sequence<T>.mute() = emptySequence<T>()
