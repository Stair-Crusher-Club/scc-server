package club.staircrusher.user.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import club.staircrusher.stdlib.di.annotation.Component

@Component
class UserBoundedContextSecurityConfig : SccSecurityConfig {
    override fun getAuthenticatedUrls() = listOf("/updateUserInfo")
}
