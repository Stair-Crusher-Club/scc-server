package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.DeleteAccessibilityPostRequest
import club.staircrusher.place.application.port.`in`.accessibility.DeleteAccessibilityUseCase
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class DeleteAccessibilityController(
    private val deleteAccessibilityUseCase: DeleteAccessibilityUseCase,
) {
    @PostMapping("/deleteAccessibility")
    fun deleteAccessibility(
        @RequestBody request: DeleteAccessibilityPostRequest,
        authentication: SccAppAuthentication,
    ) : ResponseEntity<Unit> {
        deleteAccessibilityUseCase.handle(
            userId = authentication.principal,
            placeAccessibilityId = request.placeAccessibilityId,
        )
        return ResponseEntity
            .noContent()
            .build()
    }
}
