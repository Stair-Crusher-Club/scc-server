package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.DeletePlaceReviewPostRequest
import club.staircrusher.api.spec.dto.DeleteToiletReviewPostRequest
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.api.spec.dto.PlaceReviewDto
import club.staircrusher.api.spec.dto.RegisterPlaceReviewPost200Response
import club.staircrusher.api.spec.dto.RegisterPlaceReviewRequestDto
import club.staircrusher.api.spec.dto.RegisterToiletReviewPost200Response
import club.staircrusher.api.spec.dto.RegisterToiletReviewRequestDto
import club.staircrusher.api.spec.dto.ToiletReviewDto
import club.staircrusher.place.application.port.`in`.accessibility.place_review.DeletePlaceReviewUseCase
import club.staircrusher.place.application.port.`in`.accessibility.place_review.ListPlaceReviewsUseCase
import club.staircrusher.place.application.port.`in`.accessibility.place_review.RegisterPlaceReviewUseCase
import club.staircrusher.place.application.port.`in`.accessibility.toilet_review.DeleteToiletReviewUseCase
import club.staircrusher.place.application.port.`in`.accessibility.toilet_review.ListToiletReviewsUseCase
import club.staircrusher.place.application.port.`in`.accessibility.toilet_review.RegisterToiletReviewUseCase
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ReviewController(
    private val registerPlaceReviewUseCase: RegisterPlaceReviewUseCase,
    private val registerToiletReviewUseCase: RegisterToiletReviewUseCase,
    private val listPlaceReviewsUseCase: ListPlaceReviewsUseCase,
    private val listToiletReviewsUseCase: ListToiletReviewsUseCase,
    private val deletePlaceReviewUseCase: DeletePlaceReviewUseCase,
    private val deleteToiletReviewUseCase: DeleteToiletReviewUseCase,
) {
    @PostMapping("/registerPlaceReview")
    fun registerPlaceReview(
        @RequestBody request: RegisterPlaceReviewRequestDto,
        authentication: SccAppAuthentication,
    ): RegisterPlaceReviewPost200Response {
        val userId = authentication.principal
        val result = registerPlaceReviewUseCase.handle(request.toModel(userId))

        return RegisterPlaceReviewPost200Response(
            placeReview = result.value.toDTO(userId, result.accessibilityRegisterer),
        )
    }

    @PostMapping("/listPlaceReviews")
    fun listPlaceReviews(
        @RequestBody request: GetAccessibilityPostRequest,
        authentication: SccAppAuthentication?,
    ): List<PlaceReviewDto> {
        val userId = authentication?.principal
        return listPlaceReviewsUseCase.handle(request.placeId)
            .map { it.value.toDTO(userId, it.accessibilityRegisterer) }
    }

    @PostMapping("/deletePlaceReview")
    fun deletePlaceReview(
        @RequestBody request: DeletePlaceReviewPostRequest,
        authentication: SccAppAuthentication
    ): ResponseEntity<Unit> {
        val userId = authentication.principal
        deletePlaceReviewUseCase.handle(request.placeReviewId, userId)

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/registerToiletReview")
    fun registerToiletReview(
        @RequestBody request: RegisterToiletReviewRequestDto,
        authentication: SccAppAuthentication,
    ) : RegisterToiletReviewPost200Response {
        val userId = authentication.principal
        val result = registerToiletReviewUseCase.handle(request.toModel(userId))

        return RegisterToiletReviewPost200Response(
            toiletReview = result.value.toDTO(userId, result.accessibilityRegisterer),
        )
    }

    @PostMapping("/listToiletReviews")
    fun listToiletReviews(
        @RequestBody request: GetAccessibilityPostRequest,
        authentication: SccAppAuthentication?,
    ): List<ToiletReviewDto> {
        val userId = authentication?.principal
        return listToiletReviewsUseCase.handle(request.placeId)
            .map { it.value.toDTO(userId, it.accessibilityRegisterer) }
    }

    @PostMapping("/deleteToiletReview")
    fun deleteToiletReview(
        @RequestBody request: DeleteToiletReviewPostRequest,
        authentication: SccAppAuthentication
    ): ResponseEntity<Unit> {
        val userId = authentication.principal
        deleteToiletReviewUseCase.handle(request.toiletReviewId, userId)

        return ResponseEntity.noContent().build()
    }
}
