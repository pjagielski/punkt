package pl.pjagielski.punkt

import kotlin.math.absoluteValue

val C = 60
val D = 62
val E = 64
val F = 65
val G = 67
val A = 69
val B = 71

fun Int.sharp() = inc()
fun Int.flat() = dec()

sealed class Intervals(val intervals: List<Int>) {

    fun reversed(): Intervals = Simple(this.intervals.reversed())
    fun cycle() = cycle(*this.intervals.toTypedArray())

    private class Simple(intervals: List<Int>) : Intervals(intervals)

    object major : Intervals(listOf(2,2,1,2,2,2,1))
    object minor : Intervals(listOf(2,1,2,2,1,2,2))
}

class Degrees(vararg degrees: Int) {
    val degrees: List<Int> = degrees.toList()
}

fun degrees(vararg deg: Int) = deg.toList().map { Degrees(it) }
fun degrees(degs: Sequence<Int>) = degs.toList().map { Degrees(it) }
fun chords(vararg chords: List<Int>) = chords.toList().map { Degrees(*it.toIntArray()) }

class Scale(val from: Int, val intervals: Intervals) {

    fun low() = Scale(from - 12, intervals)
    fun high() = Scale(from + 12, intervals)

    fun note(degree: Int): Int {
        return when {
            degree > 0 -> findNote(degree, intervals.cycle(), Int::plus)
            degree < 0 -> findNote(degree, intervals.reversed().cycle(), Int::minus)
            else -> from
        }
    }

    fun phrase(degrees: List<Degrees>, durations: Sequence<Double>, at: Double = 0.0): StepSequence {
        var current = at
        val diter = durations.iterator()
        return degrees.flatMap { deg ->
            val dur = diter.next()
            val ret = deg.degrees.map { Step(current, dur, note(it)) }
            current += dur
            ret
        }.asSequence()
    }

    private fun findNote(degree: Int, intervals: Sequence<Int>, next: (Int, Int) -> Int) : Int {
        var prev = from
        return intervals
            .map { prev = next(prev, it); prev }
            .take(degree.absoluteValue)
            .last()
    }
}
