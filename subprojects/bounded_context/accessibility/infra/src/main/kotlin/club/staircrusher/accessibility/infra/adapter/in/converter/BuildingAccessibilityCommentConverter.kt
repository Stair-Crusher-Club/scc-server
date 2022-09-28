package club.staircrusher.accessibility.infra.adapter.`in`.converter

import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.User

fun BuildingAccessibilityComment.toDTO(user: User?) = club.staircrusher.api.spec.dto.BuildingAccessibilityComment(
    id = id,
    buildingId = buildingId,
    comment = comment,
    createdAt = createdAt.toDTO(),
    user = user,
)
