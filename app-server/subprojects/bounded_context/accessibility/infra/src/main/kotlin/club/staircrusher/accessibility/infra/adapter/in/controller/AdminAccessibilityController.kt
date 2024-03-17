package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.AdminDeleteBuildingAccessibilityUseCase
import club.staircrusher.accessibility.application.port.`in`.AdminDeletePlaceAccessibilityUseCase
import club.staircrusher.accessibility.application.port.`in`.AdminSearchAccessibilitiesUseCase
import club.staircrusher.admin_api.spec.dto.AdminAccessibilityDTO
import club.staircrusher.admin_api.spec.dto.AdminSearchAccessibilitiesResultDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminAccessibilityController(
    private val adminSearchAccessibilitiesUseCase: AdminSearchAccessibilitiesUseCase,
    private val adminDeletePlaceAccessibilityUseCase: AdminDeletePlaceAccessibilityUseCase,
    private val adminDeleteBuildingAccessibilityUseCase: AdminDeleteBuildingAccessibilityUseCase,
) {
    @GetMapping("/admin/accessibilities/search")
    fun searchAccessibilities(
        @RequestParam(required = false) placeName: String?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false) limit: Int?,
    ): AdminSearchAccessibilitiesResultDTO {
        val result = adminSearchAccessibilitiesUseCase.handle(
            placeName = placeName,
            cursorValue = cursor,
            limit = limit,
        )
        return AdminSearchAccessibilitiesResultDTO(
            items = result.items.map {
                AdminAccessibilityDTO(
                    placeAccessibility = it.placeAccessibility.toAdminDTO(
                        placeName = it.place.name,
                        registeredUserName = it.placeAccessibilityRegisteredUser?.nickname,
                    ),
                    buildingAccessibility = it.buildingAccessibility?.toAdminDTO(
                        buildingName = it.place.building.name,
                        registeredUserName = it.buildingAccessibilityRegisteredUser?.nickname,
                    )

                )
            },
            cursor = result.cursor,
        )
    }

    @DeleteMapping("/admin/place-accessibilities/{id}")
    fun deletePlaceAccessibility(
        @RequestParam id: String,
    ): ResponseEntity<Unit> {
        adminDeletePlaceAccessibilityUseCase.handle(placeAccessibilityId = id)
        return ResponseEntity
            .noContent()
            .build()
    }

    @DeleteMapping("/admin/building-accessibilities/{id}")
    fun deleteBuildingAccessibility(
        @RequestParam id: String,
    ): ResponseEntity<Unit> {
        adminDeleteBuildingAccessibilityUseCase.handle(buildingAccessibilityId = id)
        return ResponseEntity
            .noContent()
            .build()
    }
}
