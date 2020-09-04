package pl.pjagielski.punkt

import com.illposed.osc.OSCMessage
import com.uchuhimo.konf.Config
import mu.KotlinLogging
import pl.pjagielski.punkt.config.Configuration
import pl.pjagielski.punkt.config.MidiConfig
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.jam.*
import pl.pjagielski.punkt.osc.OscServer
import pl.pjagielski.punkt.param.emptyParamMap
import pl.pjagielski.punkt.sounds.Loops
import pl.pjagielski.punkt.sounds.Samples

class Application(val config: Config, val stateProvider: StateProvider) {

    private val logger = KotlinLogging.logger {}

    fun run() {
        val scHost = config[Configuration.OSC.SuperCollider.host]
        val scPort = config[Configuration.OSC.SuperCollider.port]
        val superCollider = OscServer(scHost, scPort)
        superCollider.sync()

        val midiHost = config[Configuration.OSC.MidiBridge.host]
        val midiPort = config[Configuration.OSC.MidiBridge.port]
        val midiBridge = OscServer(midiHost, midiPort)
        midiBridge.sync()

        val sampleDir = config[Configuration.Locations.samples]
        val loopDir = config[Configuration.Locations.loops]

        val samples = Samples(superCollider).apply { load(sampleDir) }
        val loops = Loops(superCollider).apply { load(loopDir) }

        val bpm = config[Configuration.Track.bpm]
        val beatsPerBar = config[Configuration.Track.beatsPerBar]
        val clock = Clock().apply { start() }
        val metronome = Metronome(clock, bpm, beatsPerBar)

        val trackCount = config[Configuration.Track.tracks]
        val tracks = (0..trackCount).map { idx -> idx to createTrack(idx, superCollider) }.toMap().let(::Tracks)
        val trackConfig = TrackConfig(bpm, beatsPerBar, metronome, tracks)

        val nudge = config[Configuration.OSC.MidiBridge.nudge]
        val midiConfig = MidiConfig(nudge)

        val state = State(trackConfig, midiConfig, tracks, emptyList())

        val jam = Jam(stateProvider, samples, loops, metronome, superCollider, midiBridge)

        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Stopping jam...")
            jam.stop()
            Thread.sleep(metronome.millisToNextBar() + 250)

            logger.info("Cleaning up track effect nodes...")
            val freeTracksPkts = tracks.asList().map { track -> OSCMessage("/g_freeAll", listOf(track.group)) }
            superCollider.sendInBundle(freeTracksPkts, runAt = metronome.nextBarAt())
            logger.info("Shutting down")
        })

        jam.start(state)

        Thread.currentThread().join()
    }

    fun createTrack(idx: Int, superCollider: OscServer): Track {
        val busId = superCollider.oscMeta.nextTrackBusId()
        val groupId = superCollider.oscMeta.nextNodeId()
        val globalFXList = mutableListOf<GlobalFX>()
        val trackGroupPkts = superCollider.group(bid = busId, gid = groupId) {
            GlobalFX.Type.values().forEach { fxType ->
                val nodeId = superCollider.oscMeta.nextNodeId()
                node(nodeId, fxType.scName)
                globalFXList.add(GlobalFX(fxType, nodeId, emptyParamMap()))

            }
            node("fxTransfer")
        }
        superCollider.sendInBundle(trackGroupPkts)

        return Track(idx, busId, groupId, globalFXList.map { it.type to it }.toMap())
    }
}

fun application(config: Config, stateProvider: StateProvider = LiveReloadingStateProvider(config)) {
    Application(config, stateProvider).run()
}
