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
            "INACCURATE_INFO", "틀린 정보가 있어요" -> AccessibilityReportReason.InaccurateInfo
            "CLOSED", "폐점된 곳이에요" -> AccessibilityReportReason.Closed
            "BAD_USER", "이 정복자를 차단할래요" -> AccessibilityReportReason.BadUser
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
