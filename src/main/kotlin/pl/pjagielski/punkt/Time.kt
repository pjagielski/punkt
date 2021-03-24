package pl.pjagielski.punkt

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class Clock {
    lateinit var startAt: LocalDateTime
    fun currentTime() = LocalDateTime.now()

    fun start() {
        startAt = currentTime().plus(150, ChronoUnit.MILLIS)
    }
}

class Metronome(val clock: Clock, var bpm: Number, var beatsPerBar: Number) {

    private val logger = KotlinLogging.logger {}

    val millisPerBeat: Long
        get() = ((60.0 / bpm.toDouble()) * 1000).toLong()

    val millisPerBar: Long
        get() = (millisPerBeat.toDouble() * beatsPerBar.toDouble()).toLong()

    fun currentBar() = Math.floorDiv(Duration.between(clock.startAt, clock.currentTime().plus(200, ChronoUnit.MILLIS)).toMillis(), millisPerBar)

    fun currentBeat(bar: Int, beat: Double) = (bar * beatsPerBar.toDouble()) + beat

    fun nextBarAt(): LocalDateTime {
        val currentBar = currentBar()
        logger.debug("Current bar $currentBar")
        return clock.startAt.plus((currentBar + 1) * millisPerBar, ChronoUnit.MILLIS)
    }

    fun millisToNextBar() = Duration.between(currentTime(), nextBarAt()).toMillis()

    fun currentTime() = clock.currentTime()
}
