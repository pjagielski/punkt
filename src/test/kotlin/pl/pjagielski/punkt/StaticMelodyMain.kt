package pl.pjagielski.punkt

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import pl.pjagielski.punkt.config.Configuration
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.fx.chop
import pl.pjagielski.punkt.fx.dist
import pl.pjagielski.punkt.fx.djf
import pl.pjagielski.punkt.fx.waveDist
import pl.pjagielski.punkt.jam.GlobalFX
import pl.pjagielski.punkt.param.LFO
import pl.pjagielski.punkt.jam.StateProvider
import pl.pjagielski.punkt.melody.*
import pl.pjagielski.punkt.melody.Intervals.*
import pl.pjagielski.punkt.pattern.*

fun main() {
    val stateProvider = object: StateProvider() {
        override fun provide(config: TrackConfig): List<Note> {

            config.bpm = 100

            config.tracks[0].globalFX(GlobalFX.Type.CHORUS, "level" to 0.5)
            config.tracks[0].reverb(level = 0.75, room = 0.8, mix = 0.5)

            config.tracks[1].reverb(level = 0.75, room = 0.8, mix = 0.5)
            config.tracks[1].delay(level = 0.75, echo = 0.75, echotime = 4.0)
            config.tracks[1].comp(level = 0.65, dist = 0.8)

            config.tracks[2].reverb(level = 0.75, room = 2.0, mix = 0.8)
            config.tracks[2].delay(level = 0.75, echo = 0.75, echotime = 8.0)
            config.tracks[2].comp(level = 0.65, dist = 0.7)

            val scale = Scale(F, major)
            val pentatonic = Scale(F, pentatonic)

            return patterns(beats = 8) {
                + pentatonic.low()
                    .phrase(degrees(
                        cycle(arp(Chord.I, 12, ArpType.UP))),
                        cycle(0.25, 0.5)
                    )
                    .synth("tb303")
                    .track(0)
                    .amp(0.1)

                    .params("start" to 250)
                    .params("res" to 0.15)
//                    .params("cutoff" to LFO(250, 1500, 8))

//                    .waveDist(0.2)
                    .chop(config, 2)

//                    .djf(LFO(0.4, 0.3, 12))
                    .mute()

                + pentatonic.low()
                    .phrase(degrees(
                        cycle(0,0,1,2).map { listOf(it, null) }.flatten()),
                        cycle(1.0)
                    )
                    .synth("bass8")
                    .track(1)
                    .amp(0.5)
                    .dist(1.2)
                    .chop(config, 1)
                    .amp(cycle(0.7, 0.05, 0.7, 0.4))
                    .djf(LFO(0.4, 0.35, 8))
                    .mute()

                val progression = cycle(Chord.I)

                + scale.low()
                    .phrase(
                        chords(progression.map { listOf(null, it) }.flatten()),
                        cycle(2.0)
                    )
                    .synth("lead")
                    .amp(0.2)
                    .track(0)
//                    .waveDist(0.65)
                    .chop(config, cycle(4, 2))
                    .djf(LFO(0.25, 0.35, 8))
//                    .mute()
            }
        }
    }
    application(
        config = Config { addSpec(Configuration) }.from.yaml.resource("config.yaml"),
        stateProvider = stateProvider
    )
}
