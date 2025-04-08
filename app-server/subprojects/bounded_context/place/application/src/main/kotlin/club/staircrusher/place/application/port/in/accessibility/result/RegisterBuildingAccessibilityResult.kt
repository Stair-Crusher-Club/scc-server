package club.staircrusher.place.application.port.`in`.accessibility.result

import club.staircrusher.place.application.result.AccessibilityRegisterer
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibilityComment
import club.staircrusher.place.domain.model.place.Building

data class RegisterBuildingAccessibilityResult(
    val building: Building,
    val buildingAccessibility: BuildingAccessibility,
    val buildingAccessibilityComment: BuildingAccessibilityComment?,
    val accessibilityRegisterer: AccessibilityRegisterer?,
)
