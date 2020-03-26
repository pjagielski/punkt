package pl.pjagielski.punkt.sounds

import pl.pjagielski.punkt.osc.OscServer

data class SampleBuffer(
    override val bufNum: Int,
    override val name: String,
    override val channels: Int
) : Buffer

class Samples(superCollider: OscServer) : Sounds<SampleBuffer>(superCollider) {

    override fun createBuffer(nextBufNum: Int, filename: String, channels: Int)
            = filename to SampleBuffer(nextBufNum, filename, channels)
}
