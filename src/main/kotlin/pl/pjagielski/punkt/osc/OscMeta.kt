package pl.pjagielski.punkt.osc

class OscMeta {
    var bufId = 100
    var busId = 4
    var nodeId = 1100

    fun nextBufId() = synchronized(this) { bufId++ }

    fun nextBusId() = synchronized(this) {
        var nextBusId = busId++
        if (nextBusId > 100) {
            busId = 4
            nextBusId = busId++
        }
        nextBusId
    }

    fun nextNodeId() = synchronized(this) { nodeId++ }
}
