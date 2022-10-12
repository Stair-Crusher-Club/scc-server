package club.staircrusher.spring_web.authentication

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

internal class BeforeAuthSccAuthentication(
    private val accessToken: String
) : Authentication {
    override fun getName(): String? {
        return null
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return emptyList()
    }

    override fun getCredentials(): String {
        return accessToken
    }

    override fun getDetails(): Any {
        TODO("Not yet implemented")
    }

    override fun getPrincipal(): String {
        error("Not authenticated yet.")
    }

    override fun isAuthenticated(): Boolean {
        return false
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        error("Do not call this method. Create a new SccAuthentication instead.")
    }
}
