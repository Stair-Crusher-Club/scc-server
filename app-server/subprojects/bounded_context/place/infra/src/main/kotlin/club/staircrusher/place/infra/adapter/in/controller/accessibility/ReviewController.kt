package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.RegisterPlaceReviewPost200Response
import club.staircrusher.api.spec.dto.RegisterPlaceReviewRequestDto
import club.staircrusher.api.spec.dto.RegisterToiletReviewPost200Response
import club.staircrusher.api.spec.dto.RegisterToiletReviewRequestDto
import club.staircrusher.place.application.port.`in`.accessibility.place_review.RegisterPlaceReviewUseCase
import club.staircrusher.place.application.port.`in`.accessibility.toilet_review.RegisterToiletReviewUseCase
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ReviewController(
    private val registerPlaceReviewUseCase: RegisterPlaceReviewUseCase,
    private val registerToiletReviewUseCase: RegisterToiletReviewUseCase,
    private val transactionManager: TransactionManager,
) {
    @PostMapping("/registerPlaceReview")
    fun registerPlaceReview(
        @RequestBody request: RegisterPlaceReviewRequestDto,
        authentication: SccAppAuthentication,
    ): RegisterPlaceReviewPost200Response {
        val userId = authentication.principal

        // FIXME: lazy init 때문에 이렇게 하지만 마음에 안듦. Tx 를 useCase 에서 관리하고 싶음
        val placeReviewDto = transactionManager.doInTransaction {
            val result = registerPlaceReviewUseCase.handle(request.toModel(userId))
            result.value.toDTO(result.accessibilityRegisterer)
        }

        return RegisterPlaceReviewPost200Response(
            placeReview = placeReviewDto,
        )
    }

    @PostMapping("/registerToiletReview")
    fun registerToiletReview(
        @RequestBody request: RegisterToiletReviewRequestDto,
        authentication: SccAppAuthentication,
    ) : RegisterToiletReviewPost200Response {
        val userId = authentication.principal
        val toiletReviewDto = transactionManager.doInTransaction {
            val result = registerToiletReviewUseCase.handle(request.toModel(userId))
            result.value.toDTO(result.accessibilityRegisterer)
        }

        return RegisterToiletReviewPost200Response(
            toiletReview = toiletReviewDto,
        )
    }
}
