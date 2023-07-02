package club.staircrusher.api.converter

import club.staircrusher.stdlib.geography.Location

fun club.staircrusher.api.spec.dto.Location.toModel() = Location(lng = lng, lat = lat)

fun Location.toDTO() = club.staircrusher.api.spec.dto.Location(lng = lng, lat = lat)
