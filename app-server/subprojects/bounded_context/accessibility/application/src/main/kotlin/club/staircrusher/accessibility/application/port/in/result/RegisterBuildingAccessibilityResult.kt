package club.staircrusher.accessibility.application.port.`in`.result

import club.staircrusher.accessibility.application.AccessibilityRegisterer
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.place.domain.model.Building

data class RegisterBuildingAccessibilityResult(
    val building: Building,
    val buildingAccessibility: BuildingAccessibility,
    val buildingAccessibilityComment: BuildingAccessibilityComment?,
    val accessibilityRegisterer: AccessibilityRegisterer?,
)
