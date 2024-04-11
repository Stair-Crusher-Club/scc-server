package club.staircrusher.stdlib.util.string

fun String.emptyToNull() = if (this.isBlank()) null else this
