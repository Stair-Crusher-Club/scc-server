package club.staircrusher.domain_event_api

import club.staircrusher.domain_event.dto.BuildingAddressDTO
import club.staircrusher.domain_event.dto.BuildingDTO
import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory
import club.stairsrusher.domain_event_api.dto.Building
import club.stairsrusher.domain_event_api.dto.BuildingAddress
import club.stairsrusher.domain_event_api.dto.Place

fun PlaceCategory.toPlaceCategory(): club.stairsrusher.domain_event_api.dto.PlaceCategory {
    return club.stairsrusher.domain_event_api.dto.PlaceCategory.valueOf(this.name)
}

fun Location.toLocation(): club.stairsrusher.domain_event_api.dto.Location {
    return club.stairsrusher.domain_event_api.dto.Location(
        lng = this.lng,
        lat = this.lat,
    )
}

fun BuildingAddressDTO.toBuildingAddress(): BuildingAddress {
    return BuildingAddress(
        si_do = this.siDo,
        si_gun_gu = this.siGunGu,
        eup_myeon_dong = this.eupMyeonDong,
        li = this.li,
        road_name = this.roadName,
        main_building_number = this.mainBuildingNumber,
        sub_building_number = this.subBuildingNumber,
    )
}

fun BuildingDTO.toBuilding(): Building {
    return Building(
        id = this.id,
        name = this.name,
        location = this.location.toLocation(),
        address = this.address.toBuildingAddress(),
        si_gun_gu_id = this.siGunGuId,
        eup_myeon_dong_id = this.eupMyeonDongId,
    )
}

fun PlaceDTO.toPlace(): Place {
    return Place(
        id = this.id,
        name = this.name,
        location = this.location.toLocation(),
        building = this.building?.toBuilding(),
        si_gun_gu_id = this.siGunGuId,
        eup_myeon_dong_id = this.eupMyeonDongId,
        category = this.category?.toPlaceCategory(),
    )
}

fun club.stairsrusher.domain_event_api.dto.PlaceCategory.toPlaceCategory(): PlaceCategory {
    return PlaceCategory.valueOf(this.name)
}

fun club.stairsrusher.domain_event_api.dto.Location.toLocation(): Location {
    return Location(
        lng = this.lng,
        lat = this.lat,
    )
}

fun BuildingAddress.toBuildingAddressDTO(): BuildingAddressDTO {
    return BuildingAddressDTO(
        siDo = this.si_do,
        siGunGu = this.si_gun_gu,
        eupMyeonDong = this.eup_myeon_dong,
        li = this.li,
        roadName = this.road_name,
        mainBuildingNumber = this.main_building_number,
        subBuildingNumber = this.sub_building_number,
    )
}

fun Building.toBuildingDTO(): BuildingDTO {
    return BuildingDTO(
        id = this.id,
        name = this.name,
        location = this.location!!.toLocation(),
        address = this.address!!.toBuildingAddressDTO(),
        siGunGuId = this.si_gun_gu_id,
        eupMyeonDongId = this.eup_myeon_dong_id,
    )
}

fun Place.toPlaceDTO(): PlaceDTO {
    return PlaceDTO(
        id = this.id,
        name = this.name,
        location = this.location!!.toLocation(),
        building = this.building?.toBuildingDTO(),
        siGunGuId = this.si_gun_gu_id,
        eupMyeonDongId = this.eup_myeon_dong_id,
        category = this.category?.toPlaceCategory(),
    )
}
