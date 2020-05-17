package pl.pjagielski.punkt.osc

import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPacket
import pl.pjagielski.punkt.osc.Position.TAIL

enum class Position(val oscPos: Int) {
    HEAD(0), TAIL(1);
}

class Node(
    val name: String, val groupId: Int, val busId: Int,
    val nodeId: Int = -1, val position: Position = TAIL,
    val params: List<Any> = emptyList()
) {
    fun toOscPacket() = OSCMessage("/s_new", listOf(name, nodeId, position.oscPos, groupId, "bus", busId) + params)
}

class Group(val groupId: Int, val busId: Int) {

    val nodes = mutableListOf<Node>()

    fun node(name: String, position: Position = TAIL, params: List<Any> = emptyList()) {
        nodes.add(Node(name, groupId, busId, position = position, params = params))
    }

    fun toOscPackets(): List<OSCPacket> {
        val groupPkt = OSCMessage("/g_new", listOf(groupId, 1, 1))
        val nodePackets = nodes.map(Node::toOscPacket)
        return listOf(groupPkt) + nodePackets
    }

}
