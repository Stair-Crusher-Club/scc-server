package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.RegisterPlaceReviewPost200Response
import club.staircrusher.api.spec.dto.RegisterPlaceReviewRequestDto
import club.staircrusher.api.spec.dto.RegisterToiletReviewPost200Response
import club.staircrusher.api.spec.dto.RegisterToiletReviewRequestDto
import club.staircrusher.place.application.port.`in`.accessibility.place_review.RegisterPlaceReviewUseCase
import club.staircrusher.place.application.port.`in`.accessibility.toilet_review.RegisterToiletReviewUseCase
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ReviewController(
    private val registerPlaceReviewUseCase: RegisterPlaceReviewUseCase,
    private val registerToiletReviewUseCase: RegisterToiletReviewUseCase,
) {
    @PostMapping("/registerPlaceReview")
    fun registerPlaceReview(
        @RequestBody request: RegisterPlaceReviewRequestDto,
        authentication: SccAppAuthentication,
    ): RegisterPlaceReviewPost200Response {
        val userId = authentication.principal
        val result = registerPlaceReviewUseCase.handle(request.toModel(userId))

        return RegisterPlaceReviewPost200Response(
            placeReview = result.value.toDTO(result.accessibilityRegisterer),
        )
    }

    @PostMapping("/registerToiletReview")
    fun registerToiletReview(
        @RequestBody request: RegisterToiletReviewRequestDto,
        authentication: SccAppAuthentication,
    ) : RegisterToiletReviewPost200Response {
        val userId = authentication.principal
        val result = registerToiletReviewUseCase.handle(request.toModel(userId))

        return RegisterToiletReviewPost200Response(
            toiletReview = result.value.toDTO(result.accessibilityRegisterer),
        )
    }
}
