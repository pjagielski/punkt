package pl.pjagielski.punkt.pattern

data class Step(
    val beat: Double,
    val dur: Double,
    val midinote: Int = 0
) {
    fun toSample(name: String, amp: Float = 1.0f, track: Int? = null) =
        Sample(beat, dur, name, amp, track = track)

    fun toSynth(name: String, amp: Float = 1.0f, track: Int? = null) =
        Synth(beat, dur, name, midinote, amp, track = track)

    fun toLoop(name: String, beats: Float, startBeat: Float = 0.0f, amp: Float = 1.0f, track: Int? = null) =
        Loop(beat, name, beats, startBeat, amp = amp, track = track)

    fun toMidi(channel: Int) = MidiOut(beat, dur, channel, midinote)
}

typealias StepSequence = Sequence<Step>
typealias NoteSequence = Sequence<Note>

infix fun ClosedRange<Double>.step(step: Double): Iterable<Double> {
    require(start.isFinite())
    require(endInclusive.isFinite())
    require(step > 0.0) { "Step must be positive, was: $step." }
    val sequence = generateSequence(start) { previous ->
        if (previous == Double.POSITIVE_INFINITY) return@generateSequence null
        val next = previous + step
        if (next > endInclusive) null else next
    }
    return sequence.asIterable()
}

fun Sequence<Number?>.phrase(at: Double = 0.0): StepSequence {
    var current = at
    return this.map {
        val dur = it?.toDouble() ?: return@map null
        val ret = Step(current, dur)
        current += dur
        ret
    }.filterNotNull()
}

fun Sequence<Number?>.sample(smp: String, at: Double = 0.0, amp: Number = 1.0f, track: Int? = null) =
    this.phrase(at).sample(smp, amp.toFloat(), track)

fun Sequence<Number?>.loop(name: String, beats: Number, startBeat: Number = 0.0f, at: Double = 0.0, amp: Number = 1.0f, track: Int? = null) =
    this.phrase(at).loop(name, beats, startBeat, amp.toFloat(), track)

fun StepSequence.synth(name: String, amp: Number = 1.0f, track: Int? = null) =
    this.map { it.toSynth(name, amp.toFloat(), track) }

fun StepSequence.sample(name: String, amp: Number = 1.0f, track: Int? = null) =
    this.map { it.toSample(name, amp.toFloat(), track) }

fun StepSequence.loop(name: String, beats: Number, startBeat: Number = 0.0f, amp: Float = 1.0f, track: Int? = null) =
    this.map { it.toLoop(name, beats.toFloat(), startBeat.toFloat(), amp, track) }

fun StepSequence.midi(channel: Int) = this.map { it.toMidi(channel) }

fun <T : Note> Sequence<T>.beats(beats: Int) = takeWhile { it.beat < beats }.toList()

fun <T : Any> cycle(range: Iterable<T>) : Sequence<T> {
    var iter = range.iterator()
    return generateSequence {
        if (!iter.hasNext()) {
            iter = range.iterator()
        }
        iter.next()
    }
}

fun <T : Any> cycle(vararg xs: T): Sequence<T> {
    var i = 0
    return generateSequence { xs[i++ % xs.size] }
}

fun <T : Any> repeat(x: T) = generateSequence { x }

fun <T : Any> Sequence<T>.times(n: Int) = this.take(n).toList()

class PatternBuilder(val beats: Int) {

    private val sequences = mutableListOf<Sequence<Note>>()

    operator fun Sequence<Note>.unaryPlus() {
        sequences.add(this)
    }

    operator fun List<Sequence<Note>>.unaryPlus() {
        this.forEach { sequences.add(it) }
    }

    fun build() = sequences.flatMap { seq -> seq.beats(beats) }.sortedWith(compareBy(Note::beat, Note::midinote))
}

fun patterns(beats: Int, body: PatternBuilder.() -> Unit): List<Note> {
    val builder = PatternBuilder(beats)
    body.invoke(builder)
    return builder.build()
}
