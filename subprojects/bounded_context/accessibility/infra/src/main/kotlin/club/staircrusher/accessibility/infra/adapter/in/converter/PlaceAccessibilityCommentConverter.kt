package club.staircrusher.accessibility.infra.adapter.`in`.converter

import club.staircrusher.accessibility.application.UserInfo
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.api.converter.toDTO

fun PlaceAccessibilityComment.toDTO(userInfo: UserInfo?) = club.staircrusher.api.spec.dto.PlaceAccessibilityComment(
    id = id,
    placeId = placeId,
    comment = comment,
    createdAt = createdAt.toDTO(),
    user = userInfo?.toDTO(),
)
