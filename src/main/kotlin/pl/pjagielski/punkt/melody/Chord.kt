package pl.pjagielski.punkt.melody

class Chord(val root: Int, private val degrees: List<Int>) {

    fun degrees() = degrees.map { it + root}
    fun seventh() = Chord(root, degrees + 6)
    fun ninth() = Chord(root, degrees + listOf(6, 8))

    fun inversion(count: Int): Chord {
        val inverted =
            this.degrees.subList(count, this.degrees.size) +
            this.degrees.take(count).map { it + 7 }
        return Chord(root, inverted)
    }

    fun low() = Chord(root - 7, degrees)

    companion object {
        val triad = listOf(0, 2, 4)
        val I   = Chord(0, triad)
        val II  = Chord(1, triad)
        val III = Chord(2, triad)
        val IV  = Chord(3, triad)
        val V   = Chord(4, triad)
        val VI  = Chord(5, triad)
        val VII = Chord(6, triad)
    }

}
