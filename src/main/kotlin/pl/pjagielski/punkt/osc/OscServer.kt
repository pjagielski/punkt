package pl.pjagielski.punkt.osc

import com.illposed.osc.OSCBundle
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPacket
import com.illposed.osc.argument.OSCTimeTag64
import com.illposed.osc.transport.udp.OSCPortOut
import java.net.InetAddress
import java.time.LocalDateTime
import java.time.ZoneId

class OscServer(host: InetAddress, port: Int) {

    val oscMeta = OscMeta()
    val oscOut = OSCPortOut(host, port)

    fun sync() {
        synchronized(oscOut) {
            oscOut.connect()
            oscOut.send(OSCMessage("/sync"))
        }
    }

    fun sendInBundle(vararg packets: OSCPacket, runAt: LocalDateTime = LocalDateTime.now()) {
        sendInBundle(packets.toList(), runAt)
    }

    fun sendInBundle(packets: List<OSCPacket>, runAt: LocalDateTime = LocalDateTime.now()) {
        val timetag = toTimetag(runAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        val bundle = OSCBundle(packets, timetag)
        synchronized(oscOut) {
            oscOut.send(bundle)
        }
    }

    fun sendNow(packet: OSCPacket) {
        synchronized(oscOut) {
            oscOut.send(packet)
        }
    }

    fun close() {
        oscOut.close()
    }

    private fun toTimetag(theTime: Long): OSCTimeTag64 {
        val secsSince1900: Long = theTime / 1000 + TIMETAG_OFFSET
        val secsFractional = (theTime % 1000 shl 32) / 1000
        val timetag = secsSince1900 shl 32 or secsFractional
        return OSCTimeTag64.valueOf(timetag)
    }

    fun nextBufNum() = oscMeta.nextBufId()

    fun group(bid: Int? = null, gid: Int? = null, pgid: Int? = null, body: Group.() -> Unit): List<OSCPacket> {
        val groupId = gid ?: oscMeta.nextNodeId()
        val busId = bid ?: oscMeta.nextBusId()
        val parentGroupId = pgid ?: oscMeta.defaultGroupId

        val group = Group(groupId, busId, parentGroupId)
        body.invoke(group)

        return group.toOscPackets()
    }

    companion object {
        const val TIMETAG_OFFSET = 2208988800L
    }

}

