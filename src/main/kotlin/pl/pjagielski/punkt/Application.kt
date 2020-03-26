package pl.pjagielski.punkt

import com.uchuhimo.konf.Config
import mu.KotlinLogging
import pl.pjagielski.punkt.config.Configuration
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.jam.Jam
import pl.pjagielski.punkt.jam.State
import pl.pjagielski.punkt.live.loadFromScriptKSH
import pl.pjagielski.punkt.live.watchFile
import pl.pjagielski.punkt.osc.OscServer
import pl.pjagielski.punkt.pattern.Note
import pl.pjagielski.punkt.sounds.Loops
import pl.pjagielski.punkt.sounds.Samples
import java.io.File
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class Application(val config: Config) {

    private val logger = KotlinLogging.logger {}

    val scriptingHost = BasicJvmScriptingHost()

    fun run() {
        val scHost = config[Configuration.OSC.SuperCollider.host]
        val scPort = config[Configuration.OSC.SuperCollider.port]
        val superCollider = OscServer(scHost, scPort)
        superCollider.sync()

        val sampleDir = config[Configuration.Locations.samples]
        val loopDir = config[Configuration.Locations.loops]

        val samples = Samples(superCollider).apply { load(sampleDir) }
        val loops = Loops(superCollider).apply { load(loopDir) }

        val bpm = config[Configuration.Track.bpm]
        val beatsPerBar = config[Configuration.Track.beatsPerBar]

        val trackConfig = TrackConfig(bpm, beatsPerBar)
        val state = State(trackConfig, emptyList())

        val liveFile = config[Configuration.Locations.liveFile]
        val initializeState = watchFile(File(liveFile)) { file ->
            val start = System.currentTimeMillis()
            logger.info("Reloading file...")
            val func =
                loadFromScriptKSH<() -> List<Note>>(file, scriptingHost)
            val stop = System.currentTimeMillis()
            logger.info("Reloading took ${stop - start}ms")
            state.notes = func.invoke()
        }

        initializeState()

        val clock = DefaultClock()
        val metronome = Metronome(clock)

        val jam = Jam(samples, loops, metronome, superCollider)
        jam.start(state)

        Thread.currentThread().join()
    }
}

fun application(config: Config) {
    Application(config).run()
}
