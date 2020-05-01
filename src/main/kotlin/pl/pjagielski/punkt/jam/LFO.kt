package pl.pjagielski.punkt.jam

data class LFO(val min: Double, val max: Double, val length: Double = 1.0, val startBeat: Double = 0.0) {

    constructor(min: Number, max: Number, length: Number, startBeat: Double = 0.0) : this(min.toDouble(), max.toDouble(), length.toDouble(), startBeat)

    fun value(beat: Double): Double {
        val scale = (max - min) / 2 // -1 .. 1
        val offset = min + scale
        return (Math.sin(Math.PI/2 * 1.0/length * (beat - startBeat)) * scale) + offset
    }

    enum class Type {
        SIN
    }
}
