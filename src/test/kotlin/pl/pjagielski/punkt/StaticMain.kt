package pl.pjagielski.punkt

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import pl.pjagielski.punkt.config.Configuration
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.jam.StateProvider
import pl.pjagielski.punkt.pattern.*

fun main() {
    val stateProvider = object: StateProvider() {
        override fun provide(config: TrackConfig): List<Note> {
            config.tracks[0].reverb(level = 0.0, drywet = 0.000001, damp = 0.00001)
//            config.tracks[0].globalFX(CHORUS, "level" to 0.5)
            config.tracks[1].delay(level = 0.5, echo = 0.75)

            return patterns(beats = 8) {
                + repeat(1.0).sample("bd_haus")
                    .amp(0.7)

                + cycle(0.75, 1.25).sample("claps", at = 0.75)
                    .amp(0.7)
//                    .fx("djf", "cutoff" to LFO(0.3, 0.7, 4))
//                    .mute()
            }
        }
    }
    application(
        config = Config { addSpec(Configuration) }.from.yaml.resource("config.yaml"),
        stateProvider = stateProvider
    )
}
