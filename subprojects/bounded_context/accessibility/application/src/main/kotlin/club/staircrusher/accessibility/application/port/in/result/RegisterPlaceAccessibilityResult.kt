package club.staircrusher.accessibility.application.port.`in`.result

import club.staircrusher.accessibility.application.UserInfo
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment

data class RegisterPlaceAccessibilityResult(
    val placeAccessibility: PlaceAccessibility,
    val placeAccessibilityComment: PlaceAccessibilityComment?,
    val userInfo: UserInfo?,
    val registrationOrder: Int, // n번째 정복자를 표현하기 위한 값.
    val isLastPlaceAccessibilityInBuilding: Boolean,
)
