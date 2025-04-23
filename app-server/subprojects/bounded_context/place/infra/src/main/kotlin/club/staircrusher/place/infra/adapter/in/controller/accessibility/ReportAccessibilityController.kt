package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.AccessibilityReportReason
import club.staircrusher.api.spec.dto.ReportAccessibilityPostRequest
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
        val reason = when (request.reason) {
            AccessibilityReportReason.INACCURATE_INFO -> club.staircrusher.place.domain.model.accessibility.AccessibilityReportReason.InaccurateInfo
            AccessibilityReportReason.CLOSED -> club.staircrusher.place.domain.model.accessibility.AccessibilityReportReason.Closed
            AccessibilityReportReason.BAD_USER -> club.staircrusher.place.domain.model.accessibility.AccessibilityReportReason.BadUser
            null -> club.staircrusher.place.domain.model.accessibility.AccessibilityReportReason.None
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
