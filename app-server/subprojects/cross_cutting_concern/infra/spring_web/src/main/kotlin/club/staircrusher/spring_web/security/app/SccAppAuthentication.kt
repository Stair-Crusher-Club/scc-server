package club.staircrusher.spring_web.security.app

import club.staircrusher.stdlib.auth.AuthUser
import mu.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class SccAppAuthentication(
    private val authUser: AuthUser,
) : Authentication {
    private val logger = KotlinLogging.logger {}

    override fun getName(): String? {
        return null
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        val roles = setOf("ROLE_ANONYMOUS", "ROLE_${authUser.type}")
        logger.info { "Get Authorities triggered, roles of user(${authUser.id}) is $roles" }
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
