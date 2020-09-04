package pl.pjagielski.punkt.osc

import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPacket
import pl.pjagielski.punkt.osc.Position.*

enum class Position(val oscPos: Int) {
    HEAD(0), TAIL(1);
}

class Node(
    val nodeId: Int, val name: String, val groupId: Int, val busId: Int,
    val position: Position = TAIL,
    val params: List<Any> = emptyList()
) {
    fun toOscPacket() = OSCMessage("/s_new", listOf(name, nodeId, position.oscPos, groupId, "bus", busId) + params)
}

class Group(val groupId: Int, val busId: Int, val parentGroupId: Int, val position: Position = HEAD) {

    val nodes = mutableListOf<Node>()

    fun node(name: String, position: Position = TAIL, params: List<Any> = emptyList()) {
        node(-1, name, position, params)
    }

    fun node(nodeId: Int, name: String, position: Position = TAIL, params: List<Any> = emptyList()) {
        nodes.add(Node(nodeId, name, groupId, busId, position = position, params = params))
    }

    fun toOscPackets(): List<OSCPacket> {
        val groupPkt = OSCMessage("/g_new", listOf(groupId, position.oscPos, parentGroupId))
        val nodePackets = nodes.map(Node::toOscPacket)
        return listOf(groupPkt) + nodePackets
    }

}
