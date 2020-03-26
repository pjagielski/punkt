package pl.pjagielski.punkt.sounds

import mu.KotlinLogging
import pl.pjagielski.punkt.osc.OscServer

data class LoopBuffer(
    override val bufNum: Int,
    override val name: String,
    override val channels: Int,
    val bpm: Int,
    val beats: Int
) : Buffer

class Loops(superCollider: OscServer) : Sounds<LoopBuffer>(superCollider) {

    companion object {
        val loopPattern = Regex("(\\d+)_(\\d+)_(.*)")
    }

    private val logger = KotlinLogging.logger {}

    override fun createBuffer(nextBufNum: Int, filename: String, channels: Int): Pair<String, LoopBuffer>? {
        val matchResult = loopPattern.find(filename)
        if (matchResult == null) {
            logger.warn("Not matched $filename")
            return null
        }
        val (bpm, beats, name) = matchResult.destructured
        return name to LoopBuffer(nextBufNum, name, channels, bpm.toInt(), beats.toInt())
    }
}
