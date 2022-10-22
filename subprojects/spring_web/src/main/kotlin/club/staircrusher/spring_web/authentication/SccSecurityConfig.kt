package club.staircrusher.spring_web.authentication

interface SccSecurityConfig {
    fun getAuthenticatedUrls(): List<String>
}
