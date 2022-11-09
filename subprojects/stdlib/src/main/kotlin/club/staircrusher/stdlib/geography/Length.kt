package club.staircrusher.stdlib.geography

data class Length(
   val meter: Double,
) {
    companion object {

        fun ofMeters(meters: Double) = Length(meters)
        fun ofMeters(meters: Int) = ofMeters(meters.toDouble())
        @Suppress("MagicNumber")
        fun ofKilometers(kilometers: Double) = Length(kilometers * 1000)
    }

    operator fun plus(other: Length) = Length(meter + other.meter)
    operator fun minus(other: Length) = Length(meter - other.meter)
    operator fun compareTo(other: Length): Int {
        return meter.compareTo(other.meter)
    }
}
