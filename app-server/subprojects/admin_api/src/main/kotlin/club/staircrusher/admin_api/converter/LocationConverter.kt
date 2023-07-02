package club.staircrusher.admin_api.converter

import club.staircrusher.admin_api.spec.dto.LocationDTO
import club.staircrusher.stdlib.geography.Location

fun LocationDTO.toModel(): Location {
    return Location(lng = lng, lat = lat)
}

fun Location.toDTO(): LocationDTO {
    return LocationDTO(lng = lng, lat = lat)
}
