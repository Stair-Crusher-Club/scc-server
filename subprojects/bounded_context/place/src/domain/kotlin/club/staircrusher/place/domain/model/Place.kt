package club.staircrusher.place.domain.model

import club.staircrusher.stdlib.geography.Location

data class Place(
    val id: String,
    val name: String,
    val location: Location,
    val building: Building,
    val siGunGuId: String,
    val eupMyeonDongId: String,
    val category: PlaceCategory? = null,
) {
    val address: BuildingAddress
        get() = building.address
}
