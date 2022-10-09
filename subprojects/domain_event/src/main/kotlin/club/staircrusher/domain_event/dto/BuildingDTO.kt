package club.staircrusher.domain_event.dto

import club.staircrusher.stdlib.geography.Location

data class BuildingDTO(
    val id: String,
    val name: String?,
    val location: Location,
    val address: BuildingAddressDTO,
    val siGunGuId: String,
    val eupMyeonDongId: String,
)
