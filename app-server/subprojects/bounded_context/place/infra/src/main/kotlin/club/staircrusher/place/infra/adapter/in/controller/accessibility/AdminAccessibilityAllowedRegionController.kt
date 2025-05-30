package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.admin_api.converter.toModel
import club.staircrusher.admin_api.spec.dto.AccessibilityAllowedRegionDTO
import club.staircrusher.admin_api.spec.dto.CreateAccessibilityAllowedRegionRequestDTO
import club.staircrusher.place.application.port.`in`.accessibility.CreateAccessibilityAllowedRegionUseCase
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityAllowedRegionRepository
import club.staircrusher.spring_web.security.admin.SccAdminAuthentication
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
    fun createAccessibilityAllowedRegion(
        @RequestBody request: CreateAccessibilityAllowedRegionRequestDTO,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) {
        createAccessibilityAllowedRegionUseCase.handle(request.name, request.boundaryVertices.map { it.toModel() })
    }

    @GetMapping("/admin/accessibilityAllowedRegions")
    fun listAllAccessibilityAllowedRegions(
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ): List<AccessibilityAllowedRegionDTO> {
        return accessibilityAllowedRegionRepository.findAll().sortedByDescending { it.createdAt }
            .map { it.toDTO() }
    }

    @GetMapping("/admin/accessibilityAllowedRegions/{regionId}")
    fun getAccessibilityAllowedRegion(
        @PathVariable regionId: String,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ): AccessibilityAllowedRegionDTO {
        return accessibilityAllowedRegionRepository.findById(regionId).get().toDTO()
    }

    @DeleteMapping("/admin/accessibilityAllowedRegions/{regionId}")
    fun deleteAccessibilityAllowedRegion(
        @PathVariable regionId: String,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) {
        return accessibilityAllowedRegionRepository.deleteById(regionId)
    }
}
