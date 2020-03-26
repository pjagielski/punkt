package pl.pjagielski.punkt.melody

sealed class Chord(
    val degrees: List<Int>
) {
    constructor(vararg degrees: Int) : this(degrees.toList())

    object I   : Chord(0, 2, 4)
    object II  : Chord(1, 3, 5)
    object III : Chord(2, 4,-1)
    object IV  : Chord(3, 5, 0)
    object V   : Chord(4,-1, 1)
    object VI  : Chord(5, 0, 2)
    object VII : Chord(-1,1, 3)
}
