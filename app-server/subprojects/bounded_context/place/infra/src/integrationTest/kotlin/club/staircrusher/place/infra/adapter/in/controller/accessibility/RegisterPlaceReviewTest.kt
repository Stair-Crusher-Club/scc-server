package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.RecommendedMobilityTypeDto
import club.staircrusher.api.spec.dto.RegisterPlaceReviewPost200Response
import club.staircrusher.api.spec.dto.RegisterPlaceReviewRequestDto
import club.staircrusher.api.spec.dto.SpaciousTypeDto
import club.staircrusher.api.spec.dto.UserMobilityToolDto
import club.staircrusher.place.application.port.out.accessibility.persistence.place_review.PlaceReviewRepository
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RegisterPlaceReviewTest : AccessibilityITBase() {
    @Autowired
    private lateinit var placeReviewRepository: PlaceReviewRepository

    @BeforeEach
    fun setUp() {
        placeReviewRepository.deleteAll()
    }

    @Test
    fun `리뷰 등록`() {
        val user = testDataGenerator.createIdentifiedUser()
        val place = testDataGenerator.createBuildingAndPlace()

        val request = RegisterPlaceReviewRequestDto(
            placeId = place.id,
            recommendedMobilityTypes = listOf(RecommendedMobilityTypeDto.MANUAL_WHEELCHAIR),
            spaciousType = SpaciousTypeDto.TIGHT,
            mobilityTool = UserMobilityToolDto.MANUAL_WHEELCHAIR,
            seatTypes = listOf("좌식", "입식"),
            orderMethods = listOf("카운터에서 주문", "테이블 오더"),
            imageUrls = listOf("example.com/image1.jpg", "example.com/image2.jpg"),
            comment = "주차 공간이 협소하지만, 접근성은 좋았습니다.",
            features = listOf("주차 공간 협소", "어쩌고", "저쩌고"),
        )

        mvc
            .sccRequest("/registerPlaceReview", request, user.account)
            .apply {
                val result = getResult(RegisterPlaceReviewPost200Response::class)

                Assertions.assertNotNull(result.placeReview)
                Assertions.assertEquals(result.placeReview!!.user.id, user.account.id)
                Assertions.assertEquals(2, result.placeReview!!.images!!.size)
            }
    }

    @Test
    fun `이미지가 없어도 등록이 된다`() {
        val user = testDataGenerator.createIdentifiedUser()
        val place = testDataGenerator.createBuildingAndPlace()

        val request = RegisterPlaceReviewRequestDto(
            placeId = place.id,
            recommendedMobilityTypes = listOf(RecommendedMobilityTypeDto.MANUAL_WHEELCHAIR),
            spaciousType = SpaciousTypeDto.TIGHT,
            mobilityTool = UserMobilityToolDto.MANUAL_WHEELCHAIR,
            seatTypes = listOf("좌식", "입식"),
            orderMethods = listOf("카운터에서 주문", "테이블 오더"),
            imageUrls = null,
            comment = "주차 공간이 협소하지만, 접근성은 좋았습니다.",
            features = listOf("주차 공간 협소", "어쩌고", "저쩌고"),
        )

        mvc
            .sccRequest("/registerPlaceReview", request, user.account)
            .apply {
                val result = getResult(RegisterPlaceReviewPost200Response::class)

                Assertions.assertNotNull(result.placeReview)
                Assertions.assertEquals(result.placeReview!!.user.id, user.account.id)
                Assertions.assertEquals(0, result.placeReview!!.images?.size)
            }
    }
}
