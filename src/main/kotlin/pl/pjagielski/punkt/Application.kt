package pl.pjagielski.punkt

import com.illposed.osc.OSCMessage
import com.uchuhimo.konf.Config
import mu.KotlinLogging
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import pl.pjagielski.punkt.config.Configuration
import pl.pjagielski.punkt.config.MidiConfig
import pl.pjagielski.punkt.config.TrackConfig
import pl.pjagielski.punkt.jam.*
import pl.pjagielski.punkt.jam.Track
import pl.pjagielski.punkt.midi.MidiNote
import pl.pjagielski.punkt.midi.isOut
import pl.pjagielski.punkt.osc.OscServer
import pl.pjagielski.punkt.param.LFO
import pl.pjagielski.punkt.param.emptyParamMap
import pl.pjagielski.punkt.pattern.Loop
import pl.pjagielski.punkt.pattern.Note
import pl.pjagielski.punkt.pattern.Synth
import pl.pjagielski.punkt.pattern.params
import pl.pjagielski.punkt.sounds.Loops
import pl.pjagielski.punkt.sounds.Samples
import java.time.LocalDateTime
import javax.sound.midi.*

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
        val midiConfig = MidiConfig(nudge, emptyList())

        val state = State(trackConfig, midiConfig, tracks, emptyList())

        val player = Player(samples, loops, metronome, superCollider, midiBridge)
        val jam = Jam(stateProvider, metronome, superCollider, player)

        val midiDevices = config[Configuration.Midi.devices]

        val midiDeviceInfos = MidiSystem.getMidiDeviceInfo()
        val outDeviceMap = product(midiDevices, midiDeviceInfos.toList())
            .filter { (name, devInfo) -> devInfo.description.contains(name) }
            .map { (name, devInfo) -> name to MidiSystem.getMidiDevice(devInfo) }
            .filter { (_, dev) -> dev.isOut() }
            .toMap()

        outDeviceMap.forEach { (name, dev) ->
            logger.info("Connecting to device $name")
            dev.transmitter.receiver = object: Receiver {
                override fun close() {
                    TODO("Not yet implemented")
                }

                override fun send(message: MidiMessage, time: Long) {
                    if (message is ShortMessage) {
                        val midinote = message.data1
                        val velocity = message.data2
                        logger.debug("MIDI message -> note: $midinote, velocity: $velocity, command: ${message.command}")
                        if (message.command == ShortMessage.NOTE_ON) {
                            val amp = (127.toDouble() / velocity.toDouble()).toFloat()
                            val note = trackConfig.midiPlayers[name]?.invoke(MidiNote(midinote, amp))
                            note?.let { player.playNote(0, it, LocalDateTime.now(), state) }
                        }
                    }
                }
            }
            dev.open()
        }

        stateProvider.start(state)

        val notesLens = Body.auto<List<Note>>().toLens()

        val app = routes(
            "/notes.json" bind GET to { request: Request ->
                val notes = stateProvider.provide(state.trackConfig)
                val lens = notesLens(notes, request)
                Response(OK).body(lens.body).headers(lens.headers)
            }
        )
        val server = ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive).then(app).asServer(Jetty(8000)).start()

        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Stopping jam...")
            jam.stop()
            Thread.sleep(metronome.millisToNextBar() + 250)

            logger.info("Cleaning up track effect nodes...")
            val freeTracksPkts = tracks.asList().map { track -> OSCMessage("/g_freeAll", listOf(track.group)) }
            superCollider.sendInBundle(freeTracksPkts, runAt = metronome.nextBarAt())
            logger.info("Shutting down server")
            server.stop()
            logger.info("Closing midi connections")
            outDeviceMap.forEach { (_, dev) -> dev.close() }
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

fun <T, U> product(c1: Collection<T>, c2: Collection<U>): List<Pair<T, U>> {
    return c1.flatMap { lhsElem -> c2.map { rhsElem -> lhsElem to rhsElem } }
}
