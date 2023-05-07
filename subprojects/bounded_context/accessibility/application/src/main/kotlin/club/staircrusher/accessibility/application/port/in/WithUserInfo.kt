package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.UserInfo

data class WithUserInfo<T>(
    val value: T,
    val userInfo: UserInfo?
)
