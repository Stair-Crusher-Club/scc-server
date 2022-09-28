package club.staircrusher.accessibility.infra.adapter.`in`.converter

import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.User

fun PlaceAccessibilityComment.toDTO(user: User?) = club.staircrusher.api.spec.dto.PlaceAccessibilityComment(
    id = id,
    placeId = placeId,
    comment = comment,
    createdAt = createdAt.toDTO(),
    user = user,
)
