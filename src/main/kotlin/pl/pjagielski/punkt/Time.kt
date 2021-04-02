package pl.pjagielski.punkt

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import pl.pjagielski.punkt.config.TrackConfig
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class Metronome(var bpm: Number, var beatsPerBar: Number) {

    private val logger = KotlinLogging.logger {}

    companion object {
        val offset = Duration.ofMillis(150)
    }

    lateinit var startAt: LocalDateTime

    fun currentTime() = LocalDateTime.now()
    fun currentTimeWithOffset() = currentTime().plus(offset)

    fun start() {
        startAt = currentTime().plus(offset)
    }

    val millisPerBeat: Long
        get() = ((60.0 / bpm.toDouble()) * 1000).toLong()

    val millisPerBar: Long
        get() = (millisPerBeat.toDouble() * beatsPerBar.toDouble()).toLong()

    fun currentBar() = Math.floorDiv(
        Duration.between(startAt, currentTimeWithOffset().plus(50, ChronoUnit.MILLIS)).toMillis(),
        millisPerBar
    )

    fun currentBeat(bar: Int, beat: Double) = (bar * beatsPerBar.toDouble()) + beat

    fun currentBeatInBar(): Int {
        val currentBarStart = startAt.plus(currentBar() * millisPerBar, ChronoUnit.MILLIS)
        val currentBarProgress =
            Duration.between(currentBarStart, currentTimeWithOffset().minus(10, ChronoUnit.NANOS)).toMillis()
        return Math.floorDiv(currentBarProgress.toInt(), (millisPerBar / beatsPerBar.toDouble()).toInt())
    }

    fun nextBarAt(): LocalDateTime {
        val currentBar = currentBar()
        logger.debug("Current bar $currentBar")
        return startAt.plus((currentBar + 1) * millisPerBar, ChronoUnit.MILLIS)
    }

    fun nextBeatAt(): LocalDateTime {
        val currentBarStart = startAt.plus(currentBar() * millisPerBar, ChronoUnit.MILLIS)
        val currentBeatInBar = currentBeatInBar()
        logger.debug("Current beat in bar $currentBeatInBar")
        return currentBarStart.plus(((currentBeatInBar + 1) * millisPerBeat), ChronoUnit.MILLIS)
    }

    fun millisToNextBar() = Duration.between(currentTime(), nextBarAt()).toMillis()

    fun millisToNextBeat() = Duration.between(currentTime(), nextBeatAt()).toMillis()

}

data class TickData(
    val bar: Long,
    val beat: Double,
    val millisPerBeat: Long
)

typealias TickCallback = (TickData) -> Unit

class Ticker(
    val metro: Metronome, val config: TrackConfig,
    private var running: Boolean = false, var callback: TickCallback? = null
) {

    fun after(delayMillis: Long, function: TickCallback) {
        GlobalScope.launch {
            delay(delayMillis)
            val data = TickData(metro.currentBar(), metro.currentBeatInBar().toDouble(), config.millisPerBeat)
            function.invoke(data)
        }
    }

    fun tick(data: TickData) {
        if (running) {
            callback?.invoke(data)
            after(metro.millisToNextBeat(), this::tick)
        }
    }

    fun start() {
        running = true
        after(metro.millisToNextBeat(), this::tick)
    }

    fun stop() {
        running = false
    }
}
