package pl.pjagielski.punkt.config

import com.uchuhimo.konf.ConfigSpec
import java.net.InetAddress

abstract class OSCServerConfig(port: Int) : ConfigSpec() {
    val host by optional(InetAddress.getLoopbackAddress())
    val port by optional(port)
}

object Configuration : ConfigSpec() {

    object Locations : ConfigSpec() {
        val liveFile by optional("src/main/kotlin/live.kts")
        val samples by optional("work/samples")
        val loops by optional("work/loops")
    }

    object OSC : ConfigSpec() {
        object SuperCollider : OSCServerConfig(57110)
        object MidiBridge : OSCServerConfig(57120) {
            val nudge by optional(0.2)
        }
    }

    object Track : ConfigSpec() {
        val tracks by optional(4)
        val bpm by optional(100)
        val beatsPerBar by optional(8)
    }

    object Midi : ConfigSpec() {
        val devices by optional(emptyList<String>())
    }
}
