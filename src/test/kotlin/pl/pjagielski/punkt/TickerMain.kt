package pl.pjagielski.punkt

import io.github.oshai.kotlinlogging.KotlinLogging
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.jam.Tracks

fun main() {
    val logger = KotlinLogging.logger {}

    val metronome = Metronome(100, 4).also { it.start() }

    val config = TrackConfig(100, 8, metronome, Tracks(emptyMap()))

    val ticker = Ticker(metronome, config) { data ->
        logger.info { "TICK $data" }
    }

    ticker.start()

    Thread.currentThread().join()
}
