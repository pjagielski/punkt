package pl.pjagielski.punkt.melody

class Chord(val root: Int, private val degrees: List<Int> = triad) {

    fun degrees() = degrees.map { it + root }
    fun seventh() = Chord(root, degrees + 6)
    fun ninth() = Chord(root, degrees + listOf(6, 8))

    fun inversion(count: Int): Chord {
        val inverted =
            this.degrees.subList(count, this.degrees.size) +
            this.degrees.take(count).map { it + 7 }
        return Chord(root, inverted)
    }

    fun low() = Chord(root - 7, degrees)
    fun high() = Chord(root + 7, degrees)

    fun sus4() = Chord(root, sus4)

    companion object {
        val triad = listOf(0, 2, 4)
        val sus4 = listOf(0, 3, 4)
        val I   = Chord(0)
        val II  = Chord(1)
        val III = Chord(2)
        val IV  = Chord(3)
        val V   = Chord(4)
        val VI  = Chord(5)
        val VII = Chord(6)
    }

}
