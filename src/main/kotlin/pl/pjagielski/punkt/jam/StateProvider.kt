package pl.pjagielski.punkt.jam

import com.uchuhimo.konf.Config
import mu.KotlinLogging
import pl.pjagielski.punkt.config.Configuration
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.live.loadFromScriptKSH
import pl.pjagielski.punkt.live.watchFile
import pl.pjagielski.punkt.pattern.Note
import java.io.File
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

typealias Notes = List<Note>
typealias StateCallback = (Notes) -> Unit

abstract class StateProvider {
    abstract fun provide(config: TrackConfig): Notes

    open fun start(state: State) {}

    var onChanged: StateCallback? = null
}

class LiveReloadingStateProvider(val config: Config) : StateProvider() {

    private val scriptingHost = BasicJvmScriptingHost()

    private val logger = KotlinLogging.logger {}

    private lateinit var notes: Notes

    override fun provide(config: TrackConfig): Notes {
        return notes
    }

    override fun start(state: State) {
        val liveFile = config[Configuration.Locations.liveFile]

        val reloadState = watchFile(File(liveFile)) { file ->
            val start = System.currentTimeMillis()
            logger.info("Reloading file...")
            val func = loadFromScriptKSH<(TrackConfig) -> List<Note>>(file, scriptingHost)
            val stop = System.currentTimeMillis()
            logger.info("Reloading took ${stop - start}ms")
            notes = func.invoke(state.trackConfig)
            onChanged?.invoke(notes)
        }

        reloadState.invoke()
    }

}
