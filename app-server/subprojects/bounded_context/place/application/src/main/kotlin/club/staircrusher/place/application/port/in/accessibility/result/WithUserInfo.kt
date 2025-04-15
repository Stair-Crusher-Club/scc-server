package club.staircrusher.place.application.port.`in`.accessibility.result

import club.staircrusher.place.application.result.AccessibilityRegisterer


data class WithUserInfo<T>(
    val value: T,
    val accessibilityRegisterer: AccessibilityRegisterer?
)
