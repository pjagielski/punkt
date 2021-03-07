package pl.pjagielski.punkt.pattern

import pl.pjagielski.punkt.fx.FXMap
import pl.pjagielski.punkt.fx.WithFX
import pl.pjagielski.punkt.fx.emptyFXMap
import pl.pjagielski.punkt.param.*

interface Note {
    val beat: Double
    val duration: Double
    val midinote: Int?
    val amp: Float
    val track: Int?
}

data class Synth(
    override val beat: Double, override val duration: Double, val name: String, override val midinote: Int,
    override val amp: Float = 1.0f, override val params: ParamMap = emptyParamMap(), override val fxs: FXMap = emptyFXMap(),
    override val track: Int? = null
) : Note, WithFX<Synth>, WithParams<Synth> {

    override fun addFx(name: String, vararg params: Pair<String, Value>) = copy(fxs = fxs.withFX(name, *params))
    override fun addParams(vararg params: Pair<String, Value>) = copy(params = this.params.withParams(*params))
}

data class MidiOut(
    override val beat: Double, override val duration: Double, val channel: Int, override val midinote: Int,
    override val amp: Float = 1.0f, override val track: Int? = null
) : Note

data class Sample(
    override val beat: Double, override val duration: Double, val name: String,
    override val amp: Float = 1.0f, override val midinote: Int? = null,
    override val fxs: FXMap = emptyFXMap(), override val track: Int? = null
) : Note, WithFX<Sample> {

    override fun addFx(name: String, vararg params: Pair<String, Value>) = copy(fxs = fxs.withFX(name, *params))
}

data class Loop(
    override val beat: Double, val name: String,
    val beats: Float, val startBeat: Float = 0.0f,
    override val amp: Float = 1.0f, override val duration: Double = 0.0, override val midinote: Int? = null,
    override val fxs: FXMap = emptyFXMap(), override val track: Int? = null
) : Note, WithFX<Loop> {
    constructor(beat: Double, name: String, beats: Number, startBeat: Number = 0.0, amp: Number = 1.0f)
            : this(beat, name, beats.toFloat(), startBeat.toFloat(), amp.toFloat())

    override fun addFx(name: String, vararg params: Pair<String, Value>) = copy(fxs = fxs.withFX(name, *params))

    fun startBeat(startBeat: Number) = copy(startBeat = startBeat.toFloat())
}

//fun Sequence<Synth>.namedParams(vararg params: Pair<Param, Number>) = this.params(params.map { (param, value) -> param.lowercase() to value })
@JvmName("seqSynthParamsN") fun Sequence<Synth>.params(vararg params: Pair<String, Number>) = this.map { it.addParamsN(*params) }
@JvmName("seqSynthParamsL") fun Sequence<Synth>.params(vararg params: Pair<String, Computable>) = this.map { it.addParamsL(*params) }
@JvmName("seqSynthParamsL") fun Sequence<Synth>.param(params: Pair<String, Sequence<Number>>) =
    this.zip(params.second).map { (it, pval) -> it.addParamsN(params.first to pval) }

@JvmName("listFx") fun <T : WithFX<T>> List<Sequence<T>>.fx(name: String) = this.map { it.fx(name) }
@JvmName("listFxN") fun <T : WithFX<T>> List<Sequence<T>>.fx(name: String, vararg params: Pair<String, Number>) = this.map { it.fx(name, *params) }
@JvmName("listFxF") fun <T : WithFX<T>> List<Sequence<T>>.fx(name: String, vararg params: Pair<String, Computable>) = this.map { it.fx(name, *params) }

@JvmName("seqFx") fun <T : WithFX<T>> Sequence<T>.fx(name: String) = this.map { it.addFxN(name) }
@JvmName("seqFxN") fun <T : WithFX<T>> Sequence<T>.fx(name: String, vararg params: Pair<String, Number>) = this.map { it.addFxN(name, *params) }
@JvmName("seqFxL") fun <T : WithFX<T>> Sequence<T>.fx(name: String, vararg params: Pair<String, Computable>) = this.map { it.addFxC(name, *params) }

@JvmName("fxNSeq") fun <T : WithFX<T>> Sequence<T>.fx(name: String, param: Pair<String, Sequence<Number>>) =
    this.zip(param.second).map { (smp, value) -> smp.addFxN(name, param.first to value) }

@JvmName("fxNList") fun <T : WithFX<T>> Sequence<T>.fx(name: String, param: Pair<String, List<Number>>) =
    this.fx(name, param.first to param.second.asSequence())

@JvmName("seqSynthTrack") fun Sequence<Synth>.track(track: Int) = this.map { it.copy(track = track) }
@JvmName("listSynthTrack") fun List<Sequence<Synth>>.track(track: Int) = this.map { it.track(track) }
@JvmName("seqSampleTrack") fun Sequence<Sample>.track(track: Int) = this.map { it.copy(track = track) }
@JvmName("listSampleTrack") fun List<Sequence<Sample>>.track(track: Int) = this.map { it.track(track) }
@JvmName("seqLoopTrack") fun Sequence<Loop>.track(track: Int) = this.map { it.copy(track = track) }
@JvmName("listLoopTrack") fun List<Sequence<Loop>>.track(track: Int) = this.map { it.track(track) }

@JvmName("seqSynthAmp") fun Sequence<Synth>.amp(amp: Number) = this.map { it.copy(amp = amp.toFloat()) }
@JvmName("listSynthAmp") fun List<Sequence<Synth>>.amp(amp: Number) = this.map { it.amp(amp) }
@JvmName("seqSampleAmp") fun Sequence<Sample>.amp(amp: Number) = this.map { it.copy(amp = amp.toFloat()) }
@JvmName("listSampleAmp") fun List<Sequence<Sample>>.amp(amp: Number) = this.map { it.amp(amp) }
@JvmName("seqLoopAmp") fun Sequence<Loop>.amp(amp: Number) = this.map { it.copy(amp = amp.toFloat()) }
@JvmName("listLoopAmp") fun List<Sequence<Loop>>.amp(amp: Number) = this.map { it.amp(amp) }

@JvmName("seqSynthAmpSeq") fun Sequence<Synth>.amp(amps: Sequence<Number>) =
    this.zip(amps).map { (it, amp) -> it.copy(amp = amp.toFloat()) }
@JvmName("seqSampleAmpSeq") fun Sequence<Sample>.amp(amps: Sequence<Number>) =
    this.zip(amps).map { (it, amp) -> it.copy(amp = amp.toFloat()) }
@JvmName("seqLoopAmpSeq") fun Sequence<Loop>.amp(amps: Sequence<Number>) =
    this.zip(amps).map { (it, amp) -> it.copy(amp = amp.toFloat()) }

fun <T : Note> Sequence<T>.mute() = emptySequence<T>()
fun <T : Note> List<Sequence<T>>.mute() = emptySequence<T>()
