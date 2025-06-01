package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.ReportAccessibilityPostRequest
import club.staircrusher.place.application.port.`in`.accessibility.ReportAccessibilityUseCase
import club.staircrusher.place.domain.model.accessibility.AccessibilityReportReason
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
        val reason = when (request.reason) {
            "INACCURATE_INFO" -> AccessibilityReportReason.InaccurateInfo
            "CLOSED" -> AccessibilityReportReason.Closed
            "BAD_USER" -> AccessibilityReportReason.BadUser
            else -> AccessibilityReportReason.None
        }
        reportAccessibilityUseCase.handle(
            request.placeId,
            authentication.principal,
            reason,
            request.detail,
        )
        return ResponseEntity
            .noContent()
            .build()
    }
}
