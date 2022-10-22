package club.staircrusher.spring_web.security

interface SccSecurityConfig {
    fun getAuthenticatedUrls(): List<String>
}
