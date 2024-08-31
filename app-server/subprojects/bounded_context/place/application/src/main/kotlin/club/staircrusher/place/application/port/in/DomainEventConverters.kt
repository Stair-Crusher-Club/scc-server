package club.staircrusher.place.application.port.`in`

import club.staircrusher.domain_event.dto.BuildingAddressDTO
import club.staircrusher.domain_event.dto.BuildingDTO
import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.Place


fun BuildingAddressDTO.toBuildingAddress(): BuildingAddress {
    return BuildingAddress(
        siDo = this.siDo,
        siGunGu = this.siGunGu,
        eupMyeonDong = this.eupMyeonDong,
        li = this.li,
        roadName = this.roadName,
        mainBuildingNumber = this.mainBuildingNumber,
        subBuildingNumber = this.subBuildingNumber,
    )
}

fun BuildingDTO.toBuilding(): Building {
    return Building(
        id = this.id,
        name = this.name,
        location = this.location,
        address = this.address.toBuildingAddress(),
        siGunGuId = this.siGunGuId,
        eupMyeonDongId = this.eupMyeonDongId,
    )
}

fun PlaceDTO.toPlace(): Place {
    return Place.of(
        id = this.id,
        name = this.name,
        location = this.location,
        building = this.building.toBuilding(),
        siGunGuId = this.siGunGuId,
        eupMyeonDongId = this.eupMyeonDongId,
        category = this.category,
        isClosed = this.isClosed,
        isNotAccessible = this.isNotAccessible,
    )
}
