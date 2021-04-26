package pl.pjagielski.punkt.param

import pl.pjagielski.punkt.jam.State
import kotlin.random.Random

sealed class Value {
    data class Fixed(val value: Number) : Value()
    data class Dynamic(val comp: Computable) : Value()
}

interface Computable {
    fun compute(state: State, beat: Double): Number
}

@JvmName("numbArrToValues") fun Array<out Pair<String, Number>>.toValues() = this.map { (k, v) -> k to Value.Fixed(v) }.toTypedArray()
@JvmName("compArrToValues") fun Array<out Pair<String, Computable>>.toValues() = this.map { (k, v) -> k to Value.Dynamic(v) }.toTypedArray()

class Const(val value: Number) : Computable {
    override fun compute(state: State, beat: Double) = value
}

class Random(val from: Number, val to: Number) : Computable {
    val random = Random(System.currentTimeMillis())
    override fun compute(state: State, beat: Double) = random.nextDouble(from.toDouble(), to.toDouble())
}
