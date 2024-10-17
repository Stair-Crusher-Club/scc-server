package club.staircrusher.spring_web.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.util.matcher.IpAddressMatcher

object InternalIpAddressChecker {
    private val clusterIpAddressMatcher = IpAddressMatcher("10.42.0.0/16")
    private val localIpAddressMatcher = IpAddressMatcher("127.0.0.1/32")

    fun check(request: HttpServletRequest) {
        if (
            !clusterIpAddressMatcher.matches(request)
            && !localIpAddressMatcher.matches(request)
        ) {
            throw IllegalArgumentException("Unauthorized")
        }
    }
}
