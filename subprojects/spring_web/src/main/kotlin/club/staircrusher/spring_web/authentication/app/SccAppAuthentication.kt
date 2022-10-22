package club.staircrusher.spring_web.authentication.app

import club.staircrusher.stdlib.auth.AuthUser
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class SccAppAuthentication(
    private val authUser: AuthUser,
) : Authentication {
    override fun getName(): String? {
        return null
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(GrantedAuthority { authority })
    }

    override fun getCredentials(): String {
        throw error("Do not call this method. Credential is erased.")
    }

    override fun getDetails(): AuthUser {
        return authUser
    }

    override fun getPrincipal(): String {
        return authUser.id
    }

    override fun isAuthenticated(): Boolean {
        return true
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        throw IllegalArgumentException("Do not call this method. This authentication is always authenticated.")
    }

    companion object {
        const val authority = "SCC_APP_AUTH"
    }
}
