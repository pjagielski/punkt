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

fun Int.low() = this - 12
fun Int.high() = this + 12


sealed class Intervals(val intervals: List<Int>) {

    fun reversed(): Intervals =
        Simple(this.intervals.reversed())
    fun cycle() = pl.pjagielski.punkt.pattern.cycle(*this.intervals.toTypedArray()).filterNotNull()

    private class Simple(intervals: List<Int>) : Intervals(intervals)

    object major : Intervals(listOf(2,2,1,2,2,2,1))
    object minor : Intervals(listOf(2,1,2,2,1,2,2))
    object pentatonic : Intervals(listOf(3,2,2,3,2))
}

data class Degrees(val degrees: List<Int>) {
    constructor(vararg degrees: Int) : this(degrees.toList())
}


fun degrees(degs: List<Int?>) = degs.map { it.toDegrees() }.asSequence()
fun degrees(degs: Sequence<Int?>) = degs.map { it.toDegrees() }

@JvmName("iterableListChords") fun chords(chords: Iterable<List<Int>>) = chords.map { Degrees(it) }.asSequence()
@JvmName("varargChords") fun chords(vararg chords: Chord?) = chords.map { it?.degrees()?.let(::Degrees) }.asSequence()
@JvmName("iterableChords") fun chords(chords: Iterable<Chord?>) = chords.map { it?.degrees()?.let(::Degrees) }.asSequence()
@JvmName("sequenceChords") fun chords(chords: Sequence<Chord?>) = chords.map { it?.degrees()?.let(::Degrees) }

fun Int?.toDegrees() = this?.let { Degrees(it) }
fun Iterable<Chord?>.toDegrees(): Sequence<Degrees?> = this.map { it?.degrees()?.let(::Degrees) }.asSequence()
fun Sequence<Chord?>.toDegrees(): Sequence<Degrees?> = this.map { it?.degrees()?.let(::Degrees) }

class Scale(val from: Int, val intervals: Intervals) {

    fun low() = Scale(from.low(), intervals)
    fun high() = Scale(from.high(), intervals)

    fun note(degree: Int): Int {
        return when {
            degree > 0 -> findNote(degree, intervals.cycle(), Int::plus)
            degree < 0 -> findNote(degree, intervals.reversed().cycle(), Int::minus)
            else -> from
        }
    }

    fun chord(chord: Chord): List<Int> = chord.degrees().map(this::note)

    fun phrase(degrees: Sequence<Degrees?>, durations: Sequence<Number?>, at: Double = 0.0) =
        phrase(degrees, durations.iterator(), at)

    fun phrase(degrees: Sequence<Degrees?>, durations: List<Number?>, at: Double = 0.0) =
        phrase(degrees, durations.iterator(), at)

    private fun phrase(degrees: Sequence<Degrees?>, durIt: Iterator<Number?>, at: Double = 0.0): StepSequence {
        var current = at
        return degrees.map { deg ->
            if (!durIt.hasNext()) return@map null
            durIt.next()?.let { dur ->
                val ret = deg?.degrees?.map { d -> Step(current, dur.toDouble(), note(d)) }?.asSequence()
                current += dur.toDouble()
                ret
            } ?: sequenceOf()
        }.takeWhile { it != null }.filterNotNull().flatten()
    }

    private fun findNote(degree: Int, intervals: Sequence<Int>, next: (Int, Int) -> Int) : Int {
        var prev = from
        return intervals
            .map { prev = next(prev, it); prev }
            .take(degree.absoluteValue)
            .last()
    }
}
