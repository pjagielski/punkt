package pl.pjagielski.punkt

import com.uchuhimo.konf.Config
import pl.pjagielski.punkt.config.Configuration
import pl.pjagielski.punkt.osc.OscServer
import java.net.InetAddress

fun main() = application(Config { addSpec(Configuration) })

