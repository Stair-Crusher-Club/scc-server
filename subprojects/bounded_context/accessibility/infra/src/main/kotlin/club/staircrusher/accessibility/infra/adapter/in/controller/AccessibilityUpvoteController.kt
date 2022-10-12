package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.BuildingAccessibilityUpvoteApplicationService
import club.staircrusher.api.spec.dto.GiveBuildingAccessibilityUpvotePostRequest
import club.staircrusher.spring_web.authentication.app.SccAppAuthentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityUpvoteController(
    private val buildingAccessibilityUpvoteApplicationService: BuildingAccessibilityUpvoteApplicationService,
) {
    @PostMapping("/giveBuildingAccessibilityUpvote")
    fun giveBuildingAccessibilityUpvote(
        @RequestBody request: GiveBuildingAccessibilityUpvotePostRequest,
        authentication: SccAppAuthentication,
    ): ResponseEntity<Unit> {
        buildingAccessibilityUpvoteApplicationService.giveUpvote(
            authUser = authentication.details,
            buildingAccessibilityId = request.buildingAccessibilityId,
        )
        return ResponseEntity
            .noContent()
            .build()
    }

    @PostMapping("/cancelBuildingAccessibilityUpvote")
    fun cancelBuildingAccessibilityUpvote(
        @RequestBody request: GiveBuildingAccessibilityUpvotePostRequest, // TODO: CancelBuildingAccessibilityUpvotePostRequest가 왜 안 만들어지지...
        authentication: SccAppAuthentication,
    ): ResponseEntity<Unit> {
        buildingAccessibilityUpvoteApplicationService.cancelUpvote(
            authUser = authentication.details,
            buildingAccessibilityId = request.buildingAccessibilityId,
        )
        return ResponseEntity
            .noContent()
            .build()
    }
}
