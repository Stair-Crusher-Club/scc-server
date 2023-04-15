package club.staircrusher.place.domain.model

import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory

data class Place(
    val id: String,
    val name: String,
    val location: Location,
    val building: Building,
    val siGunGuId: String?,
    val eupMyeonDongId: String?,
    val category: PlaceCategory? = null,
) {
    val address: BuildingAddress
        // FIXME
        get() = building.address

    val isAccessibilityRegistrable: Boolean
        get() {
            val addressStr = address.toString()
            return addressStr.startsWith("서울") || addressStr.startsWith("경기 성남시")
        }
}
