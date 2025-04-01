package club.staircrusher.place.infra.adapter.`in`.controller.common

import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.spring_web.cdn.SccCdn

fun AccessibilityImage.toDTO() = club.staircrusher.api.spec.dto.ImageDto(
    imageUrl = SccCdn.forAccessibilityImage(imageUrl),
    thumbnailUrl = thumbnailUrl?.let { SccCdn.forAccessibilityImage(it) },
)
