package club.staircrusher.domain_event.dto

import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory

data class PlaceDTO(
    val id: String,
    val name: String,
    val location: Location,
    val building: BuildingDTO,
    val siGunGuId: String?,
    val eupMyeonDongId: String?,
    val category: PlaceCategory? = null,
) {
    val address: BuildingAddressDTO
        // FIXME
        @Suppress("UnsafeCallOnNullableType")
        get() = building.address
}
