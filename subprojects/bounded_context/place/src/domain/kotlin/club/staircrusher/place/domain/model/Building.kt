package club.staircrusher.place.domain.model

import club.staircrusher.stdlib.geography.Location

data class Building(
    val id: String,
    val name: String?,
    val location: Location,
    val address: BuildingAddress,
    val siGunGuId: String,
    val eupMyeonDongId: String,
)
