package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.api.converter.toDTO
import club.staircrusher.place.domain.model.PlaceFavorite

fun PlaceFavorite.toDto() = club.staircrusher.api.spec.dto.PlaceFavorite(
    userId = userId,
    placeId = placeId,
    createdAt = createdAt.toDTO(),
)
