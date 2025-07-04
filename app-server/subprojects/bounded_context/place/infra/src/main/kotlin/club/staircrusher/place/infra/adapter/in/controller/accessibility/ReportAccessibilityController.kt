package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.ReportAccessibilityPostRequest
import club.staircrusher.api.spec.dto.ReportTargetType
import club.staircrusher.place.application.port.`in`.accessibility.ReportAccessibilityUseCase
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ReportAccessibilityController(
    private val reportAccessibilityUseCase: ReportAccessibilityUseCase,
) {
    @PostMapping("/reportAccessibility")
    fun reportAccessibility(
        @RequestBody request: ReportAccessibilityPostRequest,
        authentication: SccAppAuthentication,
    ): ResponseEntity<Unit> {
        reportAccessibilityUseCase.handle(
            placeId = request.placeId,
            userId = authentication.principal,
            reason = request.reason.toModel(),
            target = request.targetType?.toHumanReadableName() ?: "접근성 정보",
            detail = request.detail,
        )
        return ResponseEntity
            .noContent()
            .build()
    }

    private fun ReportTargetType.toHumanReadableName(): String {
        return when (this) {
            ReportTargetType.PLACE_ACCESSIBILITY -> "접근성 정보"
            ReportTargetType.PLACE_REVIEW -> "장소 리뷰"
            ReportTargetType.TOILET -> "화장실 리뷰"
        }
    }
}
