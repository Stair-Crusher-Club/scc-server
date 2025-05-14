package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.DeleteAccessibilityPostRequest
import club.staircrusher.api.spec.dto.DeleteBuildingAccessibilityPostRequest
import club.staircrusher.place.application.port.`in`.accessibility.DeleteBuildingAccessibilityUseCase
import club.staircrusher.place.application.port.`in`.accessibility.DeletePlaceAccessibilityUseCase
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class DeleteAccessibilityController(
    private val deletePlaceAccessibilityUseCase: DeletePlaceAccessibilityUseCase,
    private val deleteBuildingAccessibilityUseCase: DeleteBuildingAccessibilityUseCase,
) {
    @Deprecated("Use deletePlaceAccessibility or deleteBuildingAccessibility instead")
    @PostMapping("/deleteAccessibility")
    fun deleteAccessibility(
        @RequestBody request: DeleteAccessibilityPostRequest,
        authentication: SccAppAuthentication,
    ) : ResponseEntity<Unit> {
        deletePlaceAccessibilityUseCase.handle(
            userId = authentication.principal,
            placeAccessibilityId = request.placeAccessibilityId,
        )
        return ResponseEntity
            .noContent()
            .build()
    }

    @PostMapping("/deletePlaceAccessibility")
    fun deletePlaceAccessibility(
        // By default, the OpenAPI Generator re-uses DTOs that have identical fields
        // even if they are declared separately in different operations
        @RequestBody request: DeleteAccessibilityPostRequest,
        authentication: SccAppAuthentication,
    ) : ResponseEntity<Unit> {
        deletePlaceAccessibilityUseCase.handle(
            userId = authentication.principal,
            placeAccessibilityId = request.placeAccessibilityId,
        )
        return ResponseEntity
            .noContent()
            .build()
    }

    @PostMapping("/deleteBuildingAccessibility")
    fun deleteBuildingAccessibility(
        @RequestBody request: DeleteBuildingAccessibilityPostRequest,
        authentication: SccAppAuthentication,
    ) : ResponseEntity<Unit> {
        deleteBuildingAccessibilityUseCase.handle(
            userId = authentication.principal,
            buildingAccessibilityId = request.buildingAccessibilityId,
        )
        return ResponseEntity
            .noContent()
            .build()
    }
}
