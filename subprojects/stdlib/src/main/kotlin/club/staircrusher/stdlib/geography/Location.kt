package club.staircrusher.stdlib.geography

import at.kopyk.CopyExtensions

@CopyExtensions
data class Location(
    val lng: Double,
    val lat: Double,
)
