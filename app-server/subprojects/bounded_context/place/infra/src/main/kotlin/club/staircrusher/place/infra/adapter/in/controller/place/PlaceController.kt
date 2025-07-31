package club.staircrusher.place.infra.adapter.`in`.controller.place

import club.staircrusher.place.application.port.`in`.place.CreateClosedPlaceCandidatesUseCase
import club.staircrusher.spring_web.security.InternalIpAddressChecker
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PlaceController(
    private val createClosedPlaceCandidatesUseCase: CreateClosedPlaceCandidatesUseCase,
) {
    @PostMapping("/createClosedPlaceCandidates")
    fun createClosedPlaceCandidates(request: HttpServletRequest) {
        InternalIpAddressChecker.check(request)
        createClosedPlaceCandidatesUseCase.handleAsync()
    }
}
