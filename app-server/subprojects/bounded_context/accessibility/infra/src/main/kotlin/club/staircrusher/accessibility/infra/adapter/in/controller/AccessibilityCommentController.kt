package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityCommentPost200Response
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityCommentPostRequest
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityCommentPost200Response
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityCommentPostRequest
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityCommentController(
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    @PostMapping("/registerBuildingAccessibilityComment")
    fun registerBuildingAccessibilityComment(
        @RequestBody request: RegisterBuildingAccessibilityCommentPostRequest,
        authentication: SccAppAuthentication?,
    ): RegisterBuildingAccessibilityCommentPost200Response {
        val result = accessibilityApplicationService.registerBuildingAccessibilityComment(
            BuildingAccessibilityCommentRepository.CreateParams(
                buildingId = request.buildingId,
                userId = authentication?.principal,
                comment = request.comment,
            )
        )
        return RegisterBuildingAccessibilityCommentPost200Response(
            buildingAccessibilityComment = result.value.toDTO(accessibilityRegisterer = result.accessibilityRegisterer)
        )
    }

    @PostMapping("/registerPlaceAccessibilityComment")
    fun registerPlaceAccessibilityComment(
        @RequestBody request: RegisterPlaceAccessibilityCommentPostRequest,
        authentication: SccAppAuthentication?,
    ): RegisterPlaceAccessibilityCommentPost200Response {
        val result = accessibilityApplicationService.registerPlaceAccessibilityComment(
            PlaceAccessibilityCommentRepository.CreateParams(
                placeId = request.placeId,
                userId = authentication?.principal,
                comment = request.comment,
            )
        )
        return RegisterPlaceAccessibilityCommentPost200Response(
            placeAccessibilityComment = result.value.toDTO(accessibilityRegisterer = result.accessibilityRegisterer)
        )
    }
}
