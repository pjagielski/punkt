package pl.pjagielski.punkt.param

data class ParamMap(
    val paramMap: Map<String, Value> = emptyMap()
) : Map<String, Value> by paramMap {
    constructor(vararg params: Pair<String, Value>) : this(params.toMap())
    fun withParams(vararg params: Pair<String, Value>) = ParamMap(paramMap + params)
}

interface WithParams<T : WithParams<T>> {
    val params: ParamMap

    fun addParamsN(vararg params: Pair<String, Number>): T = addParams(*params.toValues())
    fun addParamsL(vararg params: Pair<String, LFO>): T = addParams(*params.toValues())
    fun addParams(vararg params: Pair<String, Value>): T
}

fun emptyParamMap() = ParamMap(emptyMap())
