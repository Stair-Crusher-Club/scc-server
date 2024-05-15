package club.staircrusher.place.application.port.`in`

import club.staircrusher.domain_event.dto.BuildingAddressDTO
import club.staircrusher.domain_event.dto.BuildingDTO
import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.Place


fun BuildingAddress.toBuildingAddressDTO(): BuildingAddressDTO {
    return BuildingAddressDTO(
        siDo = this.siDo,
        siGunGu = this.siGunGu,
        eupMyeonDong = this.eupMyeonDong,
        li = this.li,
        roadName = this.roadName,
        mainBuildingNumber = this.mainBuildingNumber,
        subBuildingNumber = this.subBuildingNumber,
    )
}

fun Building.toBuildingDTO(): BuildingDTO {
    return BuildingDTO(
        id = this.id,
        name = this.name,
        location = this.location,
        address = this.address.toBuildingAddressDTO(),
        siGunGuId = this.siGunGuId,
        eupMyeonDongId = this.eupMyeonDongId,
    )
}

fun Place.toPlaceDTO(): PlaceDTO {
    return PlaceDTO(
        id = this.id,
        name = this.name,
        location = this.location,
        building = this.building.toBuildingDTO(),
        siGunGuId = this.siGunGuId,
        eupMyeonDongId = this.eupMyeonDongId,
        category = this.category,
        isClosed = this.isClosed,
        isNotAccessible = this.isNotAccessible,
    )
}
