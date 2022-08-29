package club.staircrusher.stdlib.geography

data class Length(
   private val meter: Double
) {
    companion object {
        fun ofMeters(meters: Double) = Length(meters)
        fun ofKiloMeters(kiloMeters: Double) = Length(kiloMeters * 1000)
    }

    operator fun plus(other: Length) = Length(meter + other.meter)
    operator fun minus(other: Length) = Length(meter - other.meter)
}