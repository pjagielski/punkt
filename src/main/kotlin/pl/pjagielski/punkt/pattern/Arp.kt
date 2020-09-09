package pl.pjagielski.punkt.pattern

import kotlin.reflect.KFunction1

enum class ArpType(val nextFuncs: List<KFunction1<Int, Int>>) {

    UP(listOf(Int::inc)), DOWN(listOf(Int::dec)), UPDOWN(listOf(Int::inc, Int::dec)), DOWNUP(listOf(Int::dec, Int::inc))
}

fun arp(start: Int, arp: Int, count: Int, type: ArpType = ArpType.UP): List<Int> {

    val resolveFunc = { idx: Int -> type.nextFuncs[idx / (arp / type.nextFuncs.size + 1)] }

    return repeat(start).take(arp)
        .foldIndexed(start to listOf(start), { idx, acc, i ->
            val (prev, list) = acc
            val next = resolveFunc(idx)(prev)
            next to list + next
        }).second.let { cycle(it) }.take(count).toList()
}
