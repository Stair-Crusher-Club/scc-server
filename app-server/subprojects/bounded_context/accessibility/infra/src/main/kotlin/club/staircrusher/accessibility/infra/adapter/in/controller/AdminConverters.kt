package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.domain.model.AccessibilityAllowedRegion
import club.staircrusher.admin_api.converter.toDTO
import club.staircrusher.admin_api.spec.dto.AccessibilityAllowedRegionDTO

fun AccessibilityAllowedRegion.toDTO() = AccessibilityAllowedRegionDTO(
    id = id,
    boundaryVertices = boundaryVertices.map { it.toDTO() },
    name = name,
)
