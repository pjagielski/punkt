package pl.pjagielski.punkt

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import pl.pjagielski.punkt.config.Configuration
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.fx.chop
import pl.pjagielski.punkt.fx.dist
import pl.pjagielski.punkt.fx.djf
import pl.pjagielski.punkt.param.LFO
import pl.pjagielski.punkt.jam.StateProvider
import pl.pjagielski.punkt.melody.*
import pl.pjagielski.punkt.melody.Intervals.*
import pl.pjagielski.punkt.pattern.*

fun main() {
    val stateProvider = object: StateProvider {
        override fun provide(config: TrackConfig): List<Note> {
            config.tracks[0].reverb(level = 0.5, mix = 0.4, room = 2.0)
            config.tracks[0].delay(level = 0.5, echo = 0.75)
            config.tracks[1].delay(level = 0.5, echo = 0.75)

            val scale = Scale(F, major)
            val progression = listOf(Chord.IV, Chord.IV, Chord.I, Chord.V)

            val lw = { ch: Chord -> ch.degrees[2] }

            return patterns(beats = 8) {
                + repeat(4.0).loop("oliver-dl", 4, amp = 0.75f, track = 1)
                    .mute()

                + repeat(8.0).loop("sawka-lbs", 8, startBeat = 8, amp = 0.5f, track = 1)
//                    .mute()

                + repeat(2.0).loop("amen-break", 2, amp = 0.5f, track = 1)
                    .djf(0.7)
                    .mute()

                + scale.phrase(chords(progression.flatMap { listOf(null,it,it,null,it) }), cycle(0.25, 0.25, 0.25, 0.25, 1.0))
                    .synth("lead", amp = 0.1f)
                    .dist(0.1)
                    .djf(LFO(0.4, 0.8, 8.5))
                    .chop(config, 2)
                    .mute()

                + scale.low()
                    .phrase(degrees(progression.map(lw).flatMap { listOf(it, null, it, it, null) }), cycle(0.25, 0.5, 0.25, 0.5, 1.5))
//            .phrase(degrees(cycle(0, 1, 2, 3, 2, 1)), cycle(0.25, 0.25, 0.25))
                    .synth("bass8")
                    .amp(0.1)
                    .track(0)
                    .dist(0.5)
//                    .mute()

                + scale.low()
                    .phrase(degrees(cycle(cycle(0, 1, 2, 0, 2, 1).take(10).toList())), cycle(0.25, 0.5, 0.25, 0.25, 0.5, 0.25, 0.75))
                    .synth("tb303", amp = 0.1f, track = 1)
                    .params("sus" to 0.05, "dec" to 0.25, "res" to 0.1)
                    .params("start" to LFO(800, 300, 16))
                    .params("cutoff" to LFO(500, 700, 8))
                    .mute()

                + scale.low()
//                    .phrase(degrees(progression.map(lw).flatMap { listOf(null, it) }), cycle(1.5, 0.5))
                    .phrase(degrees(cycle(0,1,0,1,0,1,0,1,2,3,2,1,0)), cycle(0.5, 0.25))
                    .midi(0)
//                    .mute()
            }
        }
    }
    application(
        config = Config { addSpec(Configuration) }.from.yaml.resource("config.yaml"),
        stateProvider = stateProvider
    )
}
