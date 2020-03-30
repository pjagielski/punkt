package pl.pjagielski.punkt.melody

import pl.pjagielski.punkt.pattern.Step
import pl.pjagielski.punkt.pattern.StepSequence
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

    fun reversed(): Intervals =
        Simple(this.intervals.reversed())
    fun cycle() = pl.pjagielski.punkt.pattern.cycle(*this.intervals.toTypedArray()).filterNotNull()

    private class Simple(intervals: List<Int>) : Intervals(intervals)

    object major : Intervals(listOf(2,2,1,2,2,2,1))
    object minor : Intervals(listOf(2,1,2,2,1,2,2))
    object pentatonic : Intervals(listOf(3,2,2,3,2))
}

class Degrees(val degrees: List<Int>) {
    constructor(vararg degrees: Int) : this(degrees.toList())
}

fun degrees(degs: List<Int?>) = degs.map { deg -> deg?.let { Degrees(it) } }.asSequence()
fun degrees(degs: Sequence<Int?>) = degs.map { deg -> deg?.let { Degrees(it) } }

fun chords(chords: Iterable<List<Int>>) = chords.map { Degrees(it) }.asSequence()
fun chords(chords: Iterable<Chord?>) = chords.map { it?.degrees?.let(::Degrees) }

fun Iterable<Chord?>.toDegrees(): Sequence<Degrees?> = this.map { it?.degrees?.let(::Degrees) }.asSequence()
fun Sequence<Chord?>.toDegrees(): Sequence<Degrees?> = this.map { it?.degrees?.let(::Degrees) }

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

    fun phrase(degrees: Sequence<Degrees?>, durations: Sequence<Double?>, at: Double = 0.0) =
        phrase(degrees, durations.iterator(), at)

    fun phrase(degrees: Sequence<Degrees?>, durations: List<Double?>, at: Double = 0.0) =
        phrase(degrees, durations.iterator(), at)

    private fun phrase(degrees: Sequence<Degrees?>, diter: Iterator<Double?>, at: Double = 0.0): StepSequence {
        var current = at
        return degrees.flatMap { deg ->
            if (!diter.hasNext()) return@flatMap sequenceOf<Step>()
            diter.next()?.let { dur ->
                val ret = deg?.degrees?.map { d -> Step(current, dur, note(d)) }?.asSequence()
                current += dur
                ret
            } ?: sequenceOf()
        }
    }

    private fun findNote(degree: Int, intervals: Sequence<Int>, next: (Int, Int) -> Int) : Int {
        var prev = from
        return intervals
            .map { prev = next(prev, it); prev }
            .take(degree.absoluteValue)
            .last()
    }
}
