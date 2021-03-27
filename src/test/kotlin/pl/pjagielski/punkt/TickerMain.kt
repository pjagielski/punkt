package pl.pjagielski.punkt

import mu.KotlinLogging

fun main() {
    val logger = KotlinLogging.logger {}

    val metronome = Metronome(100, 4).also { it.start() }

    val ticker = Ticker(metronome, step = 0.25) { data ->
        logger.info("TICK $data")
    }

    ticker.start()

    Thread.currentThread().join()
}
