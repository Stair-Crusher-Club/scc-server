package club.staircrusher.spring_web.app

import org.springframework.security.core.Authentication
import org.springframework.security.core.CredentialsContainer
import org.springframework.security.core.GrantedAuthority

class SccAppAuthentication(
    private var accessToken: String?,
) : Authentication, CredentialsContainer {
    private var userId: String? = null

    override fun getName(): String? {
        return null
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return emptyList()
    }

    override fun getCredentials(): String? {
        return accessToken
    }

    override fun getDetails() {
    }

    override fun getPrincipal(): String {
        return userId ?: "Not authenticated yet."
    }

    override fun isAuthenticated(): Boolean {
        return userId != null
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        throw IllegalArgumentException("Do not explicitly call this method. Call setUserInfo() instead.")
    }

    fun setUserInfo(userId: String) {
        this.userId = userId
    }

    override fun eraseCredentials() {
        // https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#servlet-authentication-authentication
        // authentication이 올바르게 처리되면 credential을 날려준다.
        accessToken = null
    }
}