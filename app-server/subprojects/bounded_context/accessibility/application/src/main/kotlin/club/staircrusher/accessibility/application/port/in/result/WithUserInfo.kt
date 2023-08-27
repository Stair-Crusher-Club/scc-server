package club.staircrusher.accessibility.application.port.`in`.result

import club.staircrusher.accessibility.application.AccessibilityRegisterer

data class WithUserInfo<T>(
    val value: T,
    val accessibilityRegisterer: AccessibilityRegisterer?
)
