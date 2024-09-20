package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.place.application.port.`in`.CreateClosedPlaceCandidatesUseCase
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PlaceController(
    private val createClosedPlaceCandidatesUseCase: CreateClosedPlaceCandidatesUseCase,
) {
    @PostMapping("/createClosedPlaceCandidates")
    fun createClosedPlaceCandidates(request: HttpServletRequest) {
        val clusterIpAddressMatcher = IpAddressMatcher("10.42.0.0/16")
        val localIpAddressMatcher = IpAddressMatcher("127.0.0.1/32")
        if (
            !clusterIpAddressMatcher.matches(request)
            && !localIpAddressMatcher.matches(request)
        ) {
            throw IllegalArgumentException("Unauthorized")
        }
        createClosedPlaceCandidatesUseCase.handle()
    }
}
