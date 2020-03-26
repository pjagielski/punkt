package pl.pjagielski.punkt.sounds

import com.illposed.osc.OSCMessage
import mu.KotlinLogging
import pl.pjagielski.punkt.osc.OscServer
import java.io.File
import javax.sound.sampled.AudioSystem

interface Buffer {
    val bufNum: Int
    val name: String
    val channels: Int
}

abstract class Sounds<T : Buffer>(val superCollider: OscServer) {

    lateinit var buffers: Map<String, T>

    private val logger = KotlinLogging.logger {}

    fun load(dirname: String) {
        logger.info("Loading ${this.javaClass.simpleName} from $dirname")
        val files = File(dirname).walk()
        buffers = files.map { sampleFile ->
            if (sampleFile.isDirectory) {
                return@map null
            }
            val audioInputStream = AudioSystem.getAudioInputStream(sampleFile)
            val channels = audioInputStream.format.channels
            val nextBufNum = superCollider.nextBufNum()
            superCollider.sendInBundle(OSCMessage("/b_allocRead", listOf(nextBufNum, sampleFile.absolutePath)))
            val filename = sampleFile.nameWithoutExtension
            createBuffer(nextBufNum, filename, channels)
        }.filterNotNull().toMap()
        logger.info("Finished loading ${this.javaClass.simpleName} got ${buffers.size} items")

    }

    abstract fun createBuffer(nextBufNum: Int, filename: String, channels: Int): Pair<String, T>?

    operator fun get(name: String) = buffers[name]
}
