package club.staircrusher.home_banner.infra.adapter.`in`.controller

import club.staircrusher.admin_api.converter.toDTO
import club.staircrusher.admin_api.spec.dto.AdminBannerDTO
import club.staircrusher.home_banner.domain.model.Banner

fun Banner.toAdminDTO() = AdminBannerDTO(
    id = id,
    loggingKey = loggingKey,
    imageUrl = imageUrl,
    clickPageUrl = clickPageUrl,
    clickPageTitle = clickPageTitle,
    displayOrder = displayOrder,
    startAt = startAt?.toDTO(),
    endAt = endAt?.toDTO(),
)
