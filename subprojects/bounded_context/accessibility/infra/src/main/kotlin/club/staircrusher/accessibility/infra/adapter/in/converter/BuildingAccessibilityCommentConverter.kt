package club.staircrusher.accessibility.infra.adapter.`in`.converter

import club.staircrusher.accessibility.application.UserInfo
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.api.converter.toDTO

fun BuildingAccessibilityComment.toDTO(userInfo: UserInfo?) = club.staircrusher.api.spec.dto.BuildingAccessibilityComment(
    id = id,
    buildingId = buildingId,
    comment = comment,
    createdAt = createdAt.toDTO(),
    user = userInfo?.toDTO(),
)
