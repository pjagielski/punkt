package pl.pjagielski.punkt.fx

import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.param.*
import pl.pjagielski.punkt.pattern.fx

data class FXMap(
    val fxMap: Map<String, FX> = emptyMap()
): Map<String, FX> by fxMap {
    constructor(vararg fxs: Pair<String, FX>) : this(fxs.toMap())
    fun withFX(name: String, vararg params: Pair<String, Value>)
            = FXMap(fxMap + (name to fxMap.getOrDefault(name, FX(name)).withParams(*params)))
}

interface WithFX<T : WithFX<T>> {
    val fxs: FXMap

    fun addFxN(name: String, vararg params: Pair<String, Number>): T = addFx(name, *params.toValues())
    fun addFxC(name: String, vararg params: Pair<String, Computable>): T = addFx(name, *params.toValues())
    fun addFx(name: String, vararg params: Pair<String, Value>): T
}

fun emptyFXMap() = FXMap(emptyMap())

fun <T: WithFX<T>> T.chop(config: TrackConfig, chop: Number) = this.addFxN("chop", "sus" to config.secsPerBeat, "chop" to chop)

@JvmName("seqChop") fun <T: WithFX<T>> Sequence<T>.chop(config: TrackConfig, chop: Number) = this.map { it.chop(config, chop) }
@JvmName("seqChopSeq") fun <T: WithFX<T>> Sequence<T>.chop(config: TrackConfig, chop: Sequence<Number>) =
    this.zip(chop).map { (it, value) -> it.chop(config, value) }

@JvmName("listChop") fun <T: WithFX<T>> List<Sequence<T>>.chop(config: TrackConfig, chop: Number) = this.map { it.chop(config, chop) }

@JvmName("seqHpfN") fun <T: WithFX<T>> Sequence<T>.hpf(cutoff: Number) = this.map { it.addFxN("hpf", "cutoff" to cutoff) }
@JvmName("seqHpfL") fun <T: WithFX<T>> Sequence<T>.hpf(cutoff: Computable) = this.map { it.addFxC("hpf", "cutoff" to cutoff) }

@JvmName("listHpfN") fun <T: WithFX<T>> List<Sequence<T>>.hpf(cutoff: Number) = this.map { it.hpf(cutoff) }
@JvmName("listHpfL") fun <T: WithFX<T>> List<Sequence<T>>.hpf(cutoff: Computable) = this.map { it.hpf(cutoff) }

@JvmName("seqLpfN") fun <T: WithFX<T>> Sequence<T>.lpf(cutoff: Number, res: Number = 0.5) = this.map { it.addFxN("lpf", "cutoff" to cutoff, "res" to res) }
@JvmName("seqLpfL") fun <T: WithFX<T>> Sequence<T>.lpf(cutoff: Computable, res: Computable = Const(0.5)) = this.map { it.addFxC("lpf", "cutoff" to cutoff, "res" to res) }

@JvmName("listLpfN") fun <T: WithFX<T>> List<Sequence<T>>.lpf(cutoff: Number, res: Number) = this.map { it.lpf(cutoff, res) }
@JvmName("listLpfL") fun <T: WithFX<T>> List<Sequence<T>>.lpf(cutoff: Computable, res: Computable = Const(0.5)) = this.map { it.lpf(cutoff, res) }

@JvmName("seqDjfN") fun <T: WithFX<T>> Sequence<T>.djf(cutoff: Number) = this.map { it.addFxN("djf", "cutoff" to cutoff) }
@JvmName("seqDjfL") fun <T: WithFX<T>> Sequence<T>.djf(cutoff: Computable) = this.map { it.addFxC("djf", "cutoff" to cutoff) }

@JvmName("listDjfN") fun <T: WithFX<T>> List<Sequence<T>>.djf(cutoff: Number) = this.map { it.djf(cutoff) }
@JvmName("listDjfL") fun <T: WithFX<T>> List<Sequence<T>>.djf(cutoff: Computable) = this.map { it.djf(cutoff) }

@JvmName("seqSquizN") fun <T: WithFX<T>> Sequence<T>.squiz(ratio: Number) = this.map { it.addFxN("squiz", "ratio" to ratio) }
@JvmName("seqSquizL") fun <T: WithFX<T>> Sequence<T>.squiz(ratio: Computable) = this.map { it.addFxC("squiz", "ratio" to ratio) }

@JvmName("loopSquizN") fun <T: WithFX<T>> List<Sequence<T>>.squiz(ratio: Number) = this.map { it.squiz(ratio) }
@JvmName("loopSquizL") fun <T: WithFX<T>> List<Sequence<T>>.squiz(ratio: Computable) = this.map { it.squiz(ratio) }

@JvmName("seqWaveDistN") fun <T: WithFX<T>> Sequence<T>.waveDist(shape: Number) = this.map { it.addFxN("waveDist", "shape" to shape) }
@JvmName("seqWaveDistL") fun <T: WithFX<T>> Sequence<T>.waveDist(shape: Computable) = this.map { it.addFxC("waveDist", "shape" to shape) }

@JvmName("listWaveDistN") fun <T: WithFX<T>> List<Sequence<T>>.waveDist(shape: Number) = this.map { it.waveDist(shape) }
@JvmName("listWaveDistL") fun <T: WithFX<T>> List<Sequence<T>>.waveDist(shape: Computable) = this.map { it.waveDist(shape) }

@JvmName("seqDistN") fun <T: WithFX<T>> Sequence<T>.dist(drive: Number) = this.map { it.addFxN("dist", "drive" to drive) }
@JvmName("seqDistL") fun <T: WithFX<T>> Sequence<T>.dist(drive: Computable) = this.map { it.addFxC("dist", "drive" to drive) }

@JvmName("listDistN") fun <T: WithFX<T>> List<Sequence<T>>.dist(drive: Number) = this.map { it.dist(drive) }
@JvmName("listDistL") fun <T: WithFX<T>> List<Sequence<T>>.dist(drive: Computable) = this.map { it.dist(drive) }
