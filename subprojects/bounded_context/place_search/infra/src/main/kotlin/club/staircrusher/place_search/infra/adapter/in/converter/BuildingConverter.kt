package club.staircrusher.place_search.infra.adapter.`in`.converter

import club.staircrusher.place_search.domain.model.Building

fun Building.toDTO() = club.staircrusher.api.spec.dto.Building(
    id = id,
    address = address,
)
