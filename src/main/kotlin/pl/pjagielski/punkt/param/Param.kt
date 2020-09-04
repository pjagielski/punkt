package pl.pjagielski.punkt.param

enum class Param {
    CUTOFF, RELEASE;
    fun lowercase() = this.name.toLowerCase()
}
