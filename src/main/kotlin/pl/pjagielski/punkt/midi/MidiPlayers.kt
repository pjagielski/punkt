package pl.pjagielski.punkt.midi

import pl.pjagielski.punkt.pattern.Note
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiUnavailableException

fun MidiDevice.isOut() = try {
    this.transmitter
    true
} catch (e: MidiUnavailableException) {
    false
}
data class MidiNote(
    val note: Int, val amp: Float
)

typealias MidiPlayer = (MidiNote) -> Note

typealias MidiPlayerMap = MutableMap<String, MidiPlayer>

class MidiPlayers(val players: MidiPlayerMap) {
    operator fun get(name: String) = players.get(name)
    operator fun set(name: String, player: MidiPlayer) = players.put(name, player)

    companion object {
        fun empty() = MidiPlayers(mutableMapOf())
    }
}
