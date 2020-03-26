package pl.pjagielski.punkt.pattern

data class Step(
    val beat: Double,
    val dur: Double,
    val midinote: Int = 0
) {
    fun toSample(name: String) = Sample(beat, dur, name)

    fun toSynth(name: String, amp: Float = 1.0f) =
        Synth(beat, dur, name, midinote, amp)

    fun toMidi() = MidiOut(beat, dur, "test", midinote)
}

typealias StepSequence = Sequence<Step>
typealias NoteSequence = Sequence<Note>

//[1, 1/2, 1, 1/2, 1]

//0, []
//1   -> 1, [[0,1]]
//1/2 -> 3/2, [[0,1],[1,1/2]]
//1   -> 5/2, [[0,1],[1,1/2],[3/2,1]

// 0.0
// 1   -> [0,1] c:1
// 1/2 -> [1,1/2] c:3/2
// 1   -> [3/2,1] c:5/2

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

fun Sequence<Number?>.sample(smp: String, at: Double = 0.0, amp: Float = 1.0f) =
    this.phrase(at).map { (beat, dur) ->
        Sample(
            beat,
            dur,
            smp,
            amp = amp
        )
    }

fun StepSequence.synth(name: String, amp: Float = 1.0f) = this.map { it.toSynth(name, amp = amp) }
fun StepSequence.midi() = this.map { it.toMidi() }
fun StepSequence.sample(name: String) = this.map { it.toSample(name) }

fun NoteSequence.beats(beats: Int) = takeWhile { it.beat < beats }.toList()

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

class PatternBuilder(val beats: Int) {

    private val sequences = mutableListOf<Sequence<Note>>()

    operator fun Sequence<Note>.unaryPlus() {
        sequences.add(this)
    }

    fun build() = sequences.flatMap { seq -> seq.beats(beats) }.sortedWith(compareBy(Note::beat, Note::midinote))
}

fun patterns(beats: Int, body: PatternBuilder.() -> Unit): List<Note> {
    val builder = PatternBuilder(beats)
    body.invoke(builder)
    return builder.build()
}
