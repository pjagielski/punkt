package pl.pjagielski.punkt

import com.illposed.osc.OSCMessage
import java.io.File
import javax.sound.sampled.AudioSystem

data class SampleBuffer(
    val bufNum: Int,
    val name: String,
    val channels: Int
)

class Samples {
    val dirname = "work/beats/my"

    lateinit var buffers: Map<String, SampleBuffer>

    fun load() {
        var bufNum = 1000
        val files = File(dirname).walk()
        buffers = files.map { sampleFile ->
            if (sampleFile.isDirectory) {
                return@map null
            }
            val audioInputStream = AudioSystem.getAudioInputStream(sampleFile)
            val channels = audioInputStream.format.channels
            val nextBufNum = bufNum++
            superCollider.sendInBundle(OSCMessage("/b_allocRead", listOf(nextBufNum, sampleFile.absolutePath)))
            val name = sampleFile.nameWithoutExtension
            name to SampleBuffer(nextBufNum, name, channels)
        }.filterNotNull().toMap()
    }
}
