package pl.pjagielski.punkt.osc

val MIN_TRACK_BUS_IDX = 20
val MAX_TRACK_BUS_IDX = 38
val MIN_BUS_IDX = 40
val MAX_BUS_IDX = 99
val MIN_BUF_ID = 100
val MIN_NODE_ID = 1100

class OscMeta {
    val defaultGroupId = 1

    var busIter = generateSequence(MIN_BUS_IDX) {
        it.inc().takeIf { it <= MAX_BUS_IDX } ?: MIN_BUS_IDX
    }.iterator()

    val trackBusIter = generateSequence(MIN_TRACK_BUS_IDX) {
        (it + 2).takeIf { it <= MAX_TRACK_BUS_IDX } ?: throw IllegalArgumentException("Too many tracks")
    }.iterator()

    val bufIter = generateSequence(MIN_BUF_ID) { it.inc() }.iterator()
    val nodeIter = generateSequence(MIN_NODE_ID) { it.inc() }.iterator()

    fun nextBufId() = synchronized(this) { bufIter.next() }
    fun nextBusId() = synchronized(this) { busIter.next() }
    fun nextTrackBusId() = synchronized(this) { trackBusIter.next() }
    fun nextNodeId() = synchronized(this) { nodeIter.next() }
}
