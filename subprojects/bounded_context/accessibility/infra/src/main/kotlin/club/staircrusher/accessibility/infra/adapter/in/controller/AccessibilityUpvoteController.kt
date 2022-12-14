package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.BuildingAccessibilityUpvoteApplicationService
import club.staircrusher.api.spec.dto.CancelBuildingAccessibilityUpvotePostRequest
import club.staircrusher.api.spec.dto.GiveBuildingAccessibilityUpvotePostRequest
import club.staircrusher.spring_web.security.app.SccAppAuthentication
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
            user = authentication.details,
            buildingAccessibilityId = request.buildingAccessibilityId,
        )
        return ResponseEntity
            .noContent()
            .build()
    }

    @PostMapping("/cancelBuildingAccessibilityUpvote")
    fun cancelBuildingAccessibilityUpvote(
        @RequestBody request: CancelBuildingAccessibilityUpvotePostRequest,
        authentication: SccAppAuthentication,
    ): ResponseEntity<Unit> {
        buildingAccessibilityUpvoteApplicationService.cancelUpvote(
            user = authentication.details,
            buildingAccessibilityId = request.buildingAccessibilityId,
        )
        return ResponseEntity
            .noContent()
            .build()
    }
}
