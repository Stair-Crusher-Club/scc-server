package club.staircrusher.spring_web.security.app

import club.staircrusher.stdlib.auth.AuthUser
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class SccAppAuthentication(
    private val authUser: AuthUser,
) : Authentication {
    override fun getName(): String? {
        return null
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        val roles = setOf("ROLE_ANONYMOUS", "ROLE_${authUser.type}")
        return roles.map { SimpleGrantedAuthority(it) }
    }

    override fun getCredentials(): String {
        error("Do not call this method. Credential is erased.")
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
}
