package pl.pjagielski.punkt

import java.io.File
import java.net.InetAddress
import kotlin.math.pow
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

val superCollider = OscServer(InetAddress.getLocalHost(), 57110)

val midiBridge = OscServer(InetAddress.getLoopbackAddress(), 57120)

fun midiToHz(note: Int): Float {
    return (440.0 * 2.0.pow((note - 69.0) / 12.0)).toFloat()
}

fun main() {
    val config = BasicJvmScriptingHost()

    val samples = Samples().also(Samples::load)

    val state = State(emptyList())

    val initializeState = watchFile(File("src/main/kotlin/live.kts")) { file ->
        val start = System.currentTimeMillis()
        println("Reloading file...")
        val func = loadFromScriptKSH<() -> List<Note>>(file, config)
        val stop = System.currentTimeMillis()
        println("Reloading took ${stop - start}ms")
        state.notes = func.invoke()
    }

    initializeState()

    val clock = DefaultClock()
    val metronome = Metronome(clock)

    val jam = Jam(samples, metronome)
    jam.start(state)

    Thread.currentThread().join()
}
