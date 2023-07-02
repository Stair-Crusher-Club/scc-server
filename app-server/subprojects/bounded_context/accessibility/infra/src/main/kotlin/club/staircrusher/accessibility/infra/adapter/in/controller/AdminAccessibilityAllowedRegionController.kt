package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.CreateAccessibilityAllowedRegionUseCase
import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityAllowedRegionRepository
import club.staircrusher.admin_api.converter.toModel
import club.staircrusher.admin_api.spec.dto.AccessibilityAllowedRegionDTO
import club.staircrusher.admin_api.spec.dto.CreateAccessibilityAllowedRegionRequestDTO
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminAccessibilityAllowedRegionController(
    private val createAccessibilityAllowedRegionUseCase: CreateAccessibilityAllowedRegionUseCase,
    private val accessibilityAllowedRegionRepository: AccessibilityAllowedRegionRepository,
) {
    @PostMapping("/admin/accessibilityAllowedRegions")
    fun createAccessibilityAllowedRegion(@RequestBody request: CreateAccessibilityAllowedRegionRequestDTO) {
        createAccessibilityAllowedRegionUseCase.handle(request.name, request.boundaryVertices.map { it.toModel() })
    }

    @GetMapping("/admin/accessibilityAllowedRegions")
    fun listAllAccessibilityAllowedRegions(): List<AccessibilityAllowedRegionDTO> {
        return accessibilityAllowedRegionRepository.findAll().sortedByDescending { it.createdAt }
            .map { it.toDTO() }
    }

    @GetMapping("/admin/accessibilityAllowedRegions/{regionId}")
    fun getAccessibilityAllowedRegion(@PathVariable regionId: String): AccessibilityAllowedRegionDTO {
        return accessibilityAllowedRegionRepository.findById(regionId).toDTO()
    }

    @DeleteMapping("/admin/accessibilityAllowedRegions/{regionId}")
    fun deleteAccessibilityAllowedRegion(@PathVariable regionId: String) {
        return accessibilityAllowedRegionRepository.remove(regionId)
    }
}
