package club.staircrusher.home_banner.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.HomeBannerDto
import club.staircrusher.home_banner.domain.model.Banner

fun Banner.toDTO() = HomeBannerDto(
    id = id,
    loggingKey = loggingKey,
    clickPageUrl = clickPageUrl,
    clickPageTitle = clickPageTitle,
    imageUrl = imageUrl,
)
