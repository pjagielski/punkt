package pl.pjagielski.punkt.midi

import pl.pjagielski.punkt.jam.State
import pl.pjagielski.punkt.param.Computable

typealias Channels = MutableMap<Int, Double>

class MidiState(
    val channels: Channels = mutableMapOf()
) {
    operator fun get(channel: Int) = channels.get(channel)
    operator fun set(channel: Int, value: Double) = channels.put(channel, value)
}

class MidiValue(val channel: Int, val min: Number, val max: Number): Computable {

    override fun compute(state: State, beat: Double): Number {
        val range = max.toDouble() - min.toDouble()
        val default = min.toDouble() + (range / 2.0)
        val currentRate = state.midiState[channel]
        return currentRate?.let { min.toDouble() + (range * it) } ?: default
    }
}
