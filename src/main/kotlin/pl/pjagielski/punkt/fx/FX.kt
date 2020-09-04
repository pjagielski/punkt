package pl.pjagielski.punkt.fx

import pl.pjagielski.punkt.param.ParamMap
import pl.pjagielski.punkt.param.Value
import pl.pjagielski.punkt.param.emptyParamMap

data class FX(
    val name: String,
    val params: ParamMap = emptyParamMap()
) {
    constructor(name: String, vararg params: Pair<String, Value>) : this(name, ParamMap(*params))
    fun withParams(vararg params: Pair<String, Value>) = this.copy(params = this.params.withParams(*params))
}
