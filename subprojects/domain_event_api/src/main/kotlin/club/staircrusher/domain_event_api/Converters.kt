package club.staircrusher.domain_event_api

import club.staircrusher.domain_event.dto.BuildingAddressDTO
import club.staircrusher.domain_event.dto.BuildingDTO
import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory
import club.stairsrusher.domain_event_api.proto.Building
import club.stairsrusher.domain_event_api.proto.BuildingAddress
import club.stairsrusher.domain_event_api.proto.Place

fun PlaceCategory.toProto(): club.stairsrusher.domain_event_api.proto.PlaceCategory {
    return club.stairsrusher.domain_event_api.proto.PlaceCategory.valueOf(this.name)
}

fun Location.toProto(): club.stairsrusher.domain_event_api.proto.Location {
    return club.stairsrusher.domain_event_api.proto.Location(
        lng = this.lng,
        lat = this.lat,
    )
}

fun BuildingAddressDTO.toProto(): BuildingAddress {
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

fun BuildingDTO.toProto(): Building {
    return Building(
        id = this.id,
        name = this.name,
        location = this.location.toProto(),
        address = this.address.toProto(),
        si_gun_gu_id = this.siGunGuId,
        eup_myeon_dong_id = this.eupMyeonDongId,
    )
}

fun PlaceDTO.toProto(): Place {
    return Place(
        id = this.id,
        name = this.name,
        location = this.location.toProto(),
        building = this.building?.toProto(),
        si_gun_gu_id = this.siGunGuId,
        eup_myeon_dong_id = this.eupMyeonDongId,
        category = this.category?.toProto(),
    )
}

fun club.stairsrusher.domain_event_api.proto.PlaceCategory.toDTO(): PlaceCategory {
    return PlaceCategory.valueOf(this.name)
}

fun club.stairsrusher.domain_event_api.proto.Location.toDTO(): Location {
    return Location(
        lng = this.lng,
        lat = this.lat,
    )
}

fun BuildingAddress.toDTO(): BuildingAddressDTO {
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

fun Building.toDTO(): BuildingDTO {
    return BuildingDTO(
        id = this.id,
        name = this.name,
        location = this.location!!.toDTO(),
        address = this.address!!.toDTO(),
        siGunGuId = this.si_gun_gu_id,
        eupMyeonDongId = this.eup_myeon_dong_id,
    )
}

fun Place.toDTO(): PlaceDTO {
    return PlaceDTO(
        id = this.id,
        name = this.name,
        location = this.location!!.toDTO(),
        building = this.building?.toDTO(),
        siGunGuId = this.si_gun_gu_id,
        eupMyeonDongId = this.eup_myeon_dong_id,
        category = this.category?.toDTO(),
    )
}
