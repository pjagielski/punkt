package pl.pjagielski.punkt.live

import com.sun.nio.file.SensitivityWatchEventModifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import kotlin.concurrent.thread

class FileWatcher(val file: File, private val onChange: (File) -> Unit) {
    val path = file.absoluteFile.toPath()
    val parent = path.parent
    val key = pathKeys.getOrPut(parent) {
        parent.register(
            watchService, arrayOf(StandardWatchEventKinds.ENTRY_MODIFY),
            SensitivityWatchEventModifier.HIGH
        )
    }
    val watchers = mutableListOf<() -> Unit>()

    init {
        watchThread
        watching.getOrPut(path) {
            mutableListOf()
        }.add(this)
        keyPaths.getOrPut(key) { parent }
    }

    fun stop() {
        watching[path]?.remove(this)
    }

    internal fun triggerChange() {
        onChange(file)
    }
}

private val watchers = mutableMapOf<() -> Any, FileWatcher>()

fun <T> watchFile(file: File, transducer: (File) -> T): () -> T {
    var result = transducer(file)
    val watcher = FileWatcher(file) {
        try {
            result = transducer(file)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    val function = {
        result
    }

    @Suppress("UNCHECKED_CAST")
    watchers[function as () -> Any] = watcher
    return function
}

/**
 * Stops the watcher
 */
fun <T> (() -> T).stop() {
    @Suppress("UNCHECKED_CAST")
    watchers[this as () -> Any]?.stop()

}

/**
 * Triggers reload
 */
fun <T> (() -> T).triggerChange() {
    @Suppress("UNCHECKED_CAST")
    watchers[this as () -> Any]?.triggerChange()
}

private val watching = mutableMapOf<Path, MutableList<FileWatcher>>()
private val pathKeys = mutableMapOf<Path, WatchKey>()
private val keyPaths = mutableMapOf<WatchKey, Path>()
private val waiting = mutableMapOf<Path, Job>()

private val watchService by lazy {
    FileSystems.getDefault().newWatchService()
}

private val watchThread by lazy {
    thread(isDaemon = true) {
        while (true) {
            val key = watchService.take()
            val path = keyPaths[key]
            key.pollEvents().forEach {
                val contextPath = it.context() as Path
                val fullPath = path?.resolve(contextPath)

                fullPath?.let {
                    waiting[fullPath]?.cancel()

                    waiting[fullPath] = GlobalScope.launch {
                        delay(100)
                        watching[fullPath]?.forEach { w ->
                            w.triggerChange()
                        }
                    }
                }
            }
            key.reset()
        }
    }
}
