package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.admin_api.converter.toModel
import club.staircrusher.admin_api.spec.dto.StartPlaceCrawlingRequestDTO
import club.staircrusher.place.application.port.`in`.StartPlaceCrawlingUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminPlaceController(
    private val startPlaceCrawlingUseCase: StartPlaceCrawlingUseCase,
) {
    @PostMapping("/admin/places/startCrawling")
    fun startPlaceCrawling(@RequestBody request: StartPlaceCrawlingRequestDTO) {
        startPlaceCrawlingUseCase.handle(request.boundaryVertices.map { it.toModel() })
    }
}
