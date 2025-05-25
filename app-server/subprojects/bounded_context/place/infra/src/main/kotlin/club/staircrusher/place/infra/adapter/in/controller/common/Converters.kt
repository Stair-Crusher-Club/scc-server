package club.staircrusher.place.infra.adapter.`in`.controller.common

import club.staircrusher.place.domain.model.accessibility.AccessibilityImageOld
import club.staircrusher.spring_web.cdn.SccCdn

fun AccessibilityImageOld.toDTO() = club.staircrusher.api.spec.dto.ImageDto(
    imageUrl = SccCdn.forAccessibilityImage(imageUrl),
    thumbnailUrl = thumbnailUrl?.let { SccCdn.forAccessibilityImage(it) },
)
