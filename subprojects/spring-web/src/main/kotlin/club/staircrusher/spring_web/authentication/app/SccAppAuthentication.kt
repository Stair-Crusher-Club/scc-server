package club.staircrusher.spring_web.authentication.app

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class SccAppAuthentication(
    private val userDetail: UserDetail,
) : Authentication {
    override fun getName(): String? {
        return null
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(GrantedAuthority { authority })
    }

    override fun getCredentials(): String {
        throw IllegalStateException("Do not call this method. Credential is erased.")
    }

    override fun getDetails(): UserDetail {
        return userDetail
    }

    override fun getPrincipal(): String {
        return userDetail.userId
    }

    override fun isAuthenticated(): Boolean {
        return true
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        throw IllegalArgumentException("Do not call this method. This authentication is always authenticated.")
    }

    data class UserDetail(
        val userId: String,
        val nickname: String,
        val instagramId: String?,
    )

    companion object {
        const val authority = "SCC_APP_AUTH"
    }
}
