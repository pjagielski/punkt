package pl.pjagielski.punkt.pattern

import pl.pjagielski.punkt.melody.Chord
import kotlin.reflect.KFunction1

enum class ArpType(val nextFuncs: List<KFunction1<Int, Int>>) {

    UP(listOf(Int::inc)), DOWN(listOf(Int::dec)), UPDOWN(listOf(Int::inc, Int::dec)), DOWNUP(listOf(Int::dec, Int::inc))
}

@Deprecated("for backwards compatibility")
fun arp(start: Int, arp: Int, count: Int, type: ArpType = ArpType.UP): List<Int> {
    return arp((start..(start + arp) / type.nextFuncs.count()).toList(), count, type)
}

fun arp(chord: Chord, count: Int, type: ArpType = ArpType.UP): List<Int> {
    return arp(chord.degrees(), count, type)
}

fun arp(degs: List<Int>, count: Int, type: ArpType = ArpType.UP): List<Int> {
    val funcs = cycle(type.nextFuncs).take(count).toList()
    val start = ArpStep(emptyList(), 0, type.nextFuncs[0])

    return repeat(1).take(count)
        .foldIndexed(start) { idx, step, _ ->
            val (result, curIdx, curFunc) = step
            val cur = degs[curIdx]
            val nextComputed = curFunc(curIdx)
            val nextIdx = when {
                nextComputed < degs.count() && nextComputed >= 0 -> nextComputed
                nextComputed < 0 -> degs.count() - 1
                else -> 0
            }
            val funIdx = (idx + 1) * type.nextFuncs.count() / (degs.count() + 1)
            val nextFunc = funcs[funIdx]
            ArpStep(result + cur, nextIdx, nextFunc)
        }.result
}

data class ArpStep(
    val result: List<Int>,
    val curIdx: Int,
    val curFunc: KFunction1<Int, Int>
)
