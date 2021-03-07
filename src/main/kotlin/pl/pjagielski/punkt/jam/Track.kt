package pl.pjagielski.punkt.jam

import pl.pjagielski.punkt.param.Computable
import pl.pjagielski.punkt.param.Const
import pl.pjagielski.punkt.param.ParamMap
import pl.pjagielski.punkt.param.toValues

data class GlobalFX(
    val type: Type,
    val nodeId: Int,
    var params: ParamMap
) {
    enum class Type(val scName: String) {
        DELAY("globalDelay"), REVERB("globalReverb"), DJF("globalDjf"), COMP("globalCompressor"), CHORUS("globalChorus")
    }
}

typealias GlobalFXMap = Map<GlobalFX.Type, GlobalFX>

fun GlobalFXMap.asList() = this.values.toList()

data class Track(
    val idx: Int,
    val bus: Int,
    val group: Int,
    val globalFXs: GlobalFXMap
) {
    @JvmName("globalFXN")
    fun globalFX(type: GlobalFX.Type, vararg params: Pair<String, Number>) {
        globalFXs[type]?.let { globalFX ->
            globalFX.params = ParamMap(*params.toValues())
        }
    }

    @JvmName("globalFXC")
    fun globalFX(type: GlobalFX.Type, vararg params: Pair<String, Computable>) {
        globalFXs[type]?.let { globalFX ->
            globalFX.params = ParamMap(*params.toValues())
        }
    }

    fun reverb(level: Number, drywet: Number, damp: Number) {
        globalFX(GlobalFX.Type.REVERB, "level" to level, "drywet" to drywet, "damp" to damp)
    }

    fun delay(level: Number, echo: Number, echotime: Number = 2.0) {
        globalFX(GlobalFX.Type.DELAY, "level" to level, "echo" to echo, "echotime" to echotime)
    }

    fun djf(rate: Double, res: Number = 0.9) {
        globalFX(GlobalFX.Type.DJF, "cutoff" to rate, "res" to res)
    }

    fun djf(rate: Computable, res: Computable = Const(0.9)) {
        globalFX(GlobalFX.Type.DJF, "cutoff" to rate, "res" to res)
    }

    fun comp(level: Number = 1, dist: Number = 1) {
        globalFX(GlobalFX.Type.COMP, "level" to level, "dist" to dist)
    }

    fun chorus(bright: Number = 0.0, pos: Number = 0.0) {
        globalFX(GlobalFX.Type.CHORUS, "bright" to bright, "pos" to pos)
    }
}

typealias TrackMap = Map<Int, Track>

class Tracks(val tracks: TrackMap) {
    operator fun get(idx: Int) = tracks[idx] ?: throw IllegalArgumentException("Unknown track $idx")
    fun asList() = tracks.values.toList()
}
