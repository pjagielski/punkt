package pl.pjagielski.punkt.config

import com.uchuhimo.konf.ConfigSpec
import java.net.InetAddress

object Configuration : ConfigSpec() {

    object Locations : ConfigSpec() {
        val liveFile by optional("src/main/kotlin/live.kts")
        val samples by optional("work/samples")
        val loops by optional("work/loops")
    }

    object OSC : ConfigSpec() {
        object SuperCollider : ConfigSpec() {
            val host by optional(InetAddress.getLoopbackAddress())
            val port by optional(57110)
        }
    }

    object Track : ConfigSpec() {
        val bpm by optional(100)
        val beatsPerBar by optional(8)
    }
}
