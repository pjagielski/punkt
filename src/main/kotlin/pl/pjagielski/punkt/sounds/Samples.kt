package pl.pjagielski.punkt.sounds

import pl.pjagielski.punkt.osc.OscServer

data class SampleBuffer(
    override val bufNum: Int,
    override val name: String,
    override val channels: Int,
    override val length: Float
) : Buffer

class Samples(superCollider: OscServer) : Sounds<SampleBuffer>(superCollider) {

    override fun createBuffer(nextBufNum: Int, filename: String, channels: Int, length: Float)
            = filename to SampleBuffer(nextBufNum, filename, channels, length)
}
