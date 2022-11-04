package club.staircrusher.spring_web.security.admin

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class SccAdminAuthentication : Authentication {
    override fun getName(): String {
        return "Admin"
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(GrantedAuthority { "ROLE_$authority" })
    }

    override fun getCredentials(): String {
        error("Do not call this method. Credential is erased.")
    }

    override fun getDetails() {
        // No detail.
    }

    override fun getPrincipal() {
        // No principal.
    }

    override fun isAuthenticated(): Boolean {
        return true
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        throw IllegalArgumentException("Do not call this method. This authentication is always authenticated.")
    }

    companion object {
        const val authority = "SCC_ADMIN_AUTH"
    }
}
