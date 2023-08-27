package club.staircrusher.accessibility.application.port.`in`.result

import club.staircrusher.accessibility.application.AccessibilityRegisterer
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment

data class RegisterBuildingAccessibilityResult(
    val buildingAccessibility: BuildingAccessibility?,
    val buildingAccessibilityComment: BuildingAccessibilityComment?,
    val accessibilityRegisterer: AccessibilityRegisterer?,
)
