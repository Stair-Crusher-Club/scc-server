package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.AdminSearchAccessibilitiesUseCase
import club.staircrusher.admin_api.spec.dto.AdminAccessibilityDTO
import club.staircrusher.admin_api.spec.dto.AdminSearchAccessibilitiesResultDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminAccessibilityController(
    private val adminSearchAccessibilitiesUseCase: AdminSearchAccessibilitiesUseCase,
) {
    @GetMapping("/admin/accessibilities/search")
    fun searchAccessibilities(
        @RequestParam placeName: String?,
        @RequestParam cursor: String?,
        @RequestParam limit: Int?,
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
}
