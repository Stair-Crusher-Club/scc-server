package club.staircrusher.place_search.infra.adapter.`in`.converter

import club.staircrusher.place_search.domain.model.Place

fun Place.toDTO() = club.staircrusher.api.spec.dto.Place(
    id = id,
    name = name,
    address = address,
)
