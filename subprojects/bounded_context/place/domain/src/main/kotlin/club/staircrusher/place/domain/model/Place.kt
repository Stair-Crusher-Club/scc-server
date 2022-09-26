package club.staircrusher.place.domain.model

import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory

data class Place(
    val id: String,
    val name: String,
    val location: Location,
    // FIXME: make unnullable
    val building: Building?,
    val siGunGuId: String?,
    val eupMyeonDongId: String?,
    val category: PlaceCategory? = null,
) {
    val address: BuildingAddress
        // FIXME
        get() = building!!.address
}
