package pl.pjagielski.punkt.param

sealed class Value {
    data class Fixed(val value: Number) : Value()
    data class Dynamic(val comp: Computable) : Value()
}

interface Computable {
    fun compute(beat: Double): Number
}

@JvmName("numbArrToValues") fun Array<out Pair<String, Number>>.toValues() = this.map { (k, v) -> k to Value.Fixed(v) }.toTypedArray()
@JvmName("compArrToValues") fun Array<out Pair<String, Computable>>.toValues() = this.map { (k, v) -> k to Value.Dynamic(v) }.toTypedArray()
