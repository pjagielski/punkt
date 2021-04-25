package pl.pjagielski.punkt.param

import pl.pjagielski.punkt.jam.State

data class ParamMap(
    val paramMap: Map<String, Value> = emptyMap()
) : Map<String, Value> by paramMap {
    constructor(vararg params: Pair<String, Value>) : this(params.toMap())
    fun withParams(vararg params: Pair<String, Value>) = ParamMap(paramMap + params)

    fun compute(state: State, currentBeat: Double) : List<Pair<String, Float>> {
        return this.map { (k, v) ->
            val compValue = when (v) {
                is Value.Fixed -> v.value.toFloat()
                is Value.Dynamic -> v.comp.compute(state, currentBeat).toFloat()
            }
            k to compValue
        }
    }
}

interface WithParams<T : WithParams<T>> {
    val params: ParamMap

    fun addParamsN(vararg params: Pair<String, Number>): T = addParams(*params.toValues())
    fun addParamsL(vararg params: Pair<String, Computable>): T = addParams(*params.toValues())
    fun addParams(vararg params: Pair<String, Value>): T
}

fun emptyParamMap() = ParamMap(emptyMap())

