package club.staircrusher.place_search.domain.model

import club.staircrusher.stdlib.geography.Location

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val location: Location,
    val building: Building,
)
