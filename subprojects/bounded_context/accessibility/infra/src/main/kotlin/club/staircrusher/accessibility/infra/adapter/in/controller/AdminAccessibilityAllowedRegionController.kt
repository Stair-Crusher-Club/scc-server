package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.CreateAccessibilityAllowedRegionUseCase
import club.staircrusher.admin_api.converter.toModel
import club.staircrusher.admin_api.spec.dto.CreateAccessibilityAllowedRegionRequestDTO
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminAccessibilityAllowedRegionController(
    private val createAccessibilityAllowedRegionUseCase: CreateAccessibilityAllowedRegionUseCase,
) {
    @PostMapping("/admin/accessibilityAllowedRegions")
    fun createAccessibilityAllowedRegion(@RequestBody request: CreateAccessibilityAllowedRegionRequestDTO) {
        createAccessibilityAllowedRegionUseCase.handle(request.name, request.boundaryVertices.map { it.toModel() })
    }
}
