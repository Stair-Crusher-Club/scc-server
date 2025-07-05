package club.staircrusher.place.application.port.`in`.accessibility.result

import club.staircrusher.place.application.result.AccessibilityRegisterer
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibilityComment
import club.staircrusher.place.domain.model.place.Place

data class RegisterPlaceAccessibilityResult(
    val place: Place,
    val placeAccessibility: PlaceAccessibility,
    val placeAccessibilityComment: PlaceAccessibilityComment?,
    val accessibilityRegisterer: AccessibilityRegisterer?,
    val registrationOrder: Int, // n번째 정복자를 표현하기 위한 값.
    val isLastPlaceAccessibilityInBuilding: Boolean,
)
