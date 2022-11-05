package club.staircrusher.quest.infra.adapter.`in`.converter

import club.staircrusher.admin.api.dto.LocationDTO
import club.staircrusher.stdlib.geography.Location

// TODO: admin-api로 옮기기
fun LocationDTO.toModel(): Location {
    return Location(lng = lng, lat = lat)
}

fun Location.toDTO(): LocationDTO {
    return LocationDTO(lng = lng, lat = lat)
}
