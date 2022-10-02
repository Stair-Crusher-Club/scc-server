package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.AccessibilityApplicationService
import club.staircrusher.accessibility.domain.service.BuildingAccessibilityCommentService
import club.staircrusher.accessibility.domain.service.PlaceAccessibilityCommentService
import club.staircrusher.accessibility.infra.adapter.`in`.converter.toDTO
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityCommentPost200Response
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityCommentPostRequest
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityCommentPost200Response
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityCommentPostRequest
import club.staircrusher.spring_web.app.SccAppAuthentication
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
        authentication: SccAppAuthentication,
    ): RegisterBuildingAccessibilityCommentPost200Response {
        val comment = accessibilityApplicationService.registerBuildingAccessibilityComment(
            BuildingAccessibilityCommentService.CreateParams(
                buildingId = request.buildingId,
                userId = authentication.principal,
                comment = request.comment,
            )
        )
        return RegisterBuildingAccessibilityCommentPost200Response(
            buildingAccessibilityComment = comment.toDTO(user = null) // TODO: user 채우기
        )
    }

    @PostMapping("/registerPlaceAccessibilityComment")
    fun registerPlaceAccessibilityComment(
        @RequestBody request: RegisterPlaceAccessibilityCommentPostRequest,
        authentication: SccAppAuthentication,
    ): RegisterPlaceAccessibilityCommentPost200Response {
        val comment = accessibilityApplicationService.registerPlaceAccessibilityComment(
            PlaceAccessibilityCommentService.CreateParams(
                placeId = request.placeId,
                userId = authentication.principal,
                comment = request.comment,
            )
        )
        return RegisterPlaceAccessibilityCommentPost200Response(
            placeAccessibilityComment = comment.toDTO(user = null) // TODO: user 채우기
        )
    }
}
