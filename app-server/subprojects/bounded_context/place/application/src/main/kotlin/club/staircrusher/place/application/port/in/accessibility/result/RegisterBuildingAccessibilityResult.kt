package club.staircrusher.place.application.port.`in`.accessibility.result

import club.staircrusher.accessibility.application.AccessibilityRegisterer
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.place.domain.model.place.Building

data class RegisterBuildingAccessibilityResult(
    val building: Building,
    val buildingAccessibility: BuildingAccessibility,
    val buildingAccessibilityComment: BuildingAccessibilityComment?,
    val accessibilityRegisterer: AccessibilityRegisterer?,
)
