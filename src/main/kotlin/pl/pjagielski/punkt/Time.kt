package pl.pjagielski.punkt

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

interface Clock {
    fun currentTime() : LocalDateTime
}

class DefaultClock : Clock {
    override fun currentTime() = LocalDateTime.now()
}

class Metronome(val clock: Clock) {

    private val logger = KotlinLogging.logger {}

    lateinit var startAt: LocalDateTime

    val bpm = 100
    val beatsPerBar = 8

    val millisPerBeat: Long
        get() = ((60.0 / bpm) * 1000).toLong()

    val millisPerBar: Long
        get() = millisPerBeat * beatsPerBar

    fun start() {
        startAt = clock.currentTime().plus(150, ChronoUnit.MILLIS)
    }

    fun currentBar() = Math.floorDiv(Duration.between(startAt, clock.currentTime()).toMillis(), millisPerBar)

    fun currentBeat(beat: Double) = (currentBar() * beatsPerBar) + beat

    fun nextBarAt(): LocalDateTime {
        val currentBar = currentBar()
        logger.debug("Current bar $currentBar")
        return startAt.plus((currentBar + 1) * millisPerBar, ChronoUnit.MILLIS)
    }

    fun millisToNextBar() = Duration.between(currentTime(), nextBarAt()).toMillis()

    fun currentTime() = clock.currentTime()
}
