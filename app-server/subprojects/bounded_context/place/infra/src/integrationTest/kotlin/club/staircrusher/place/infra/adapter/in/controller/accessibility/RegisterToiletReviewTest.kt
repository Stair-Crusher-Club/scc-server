package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.EntranceDoorType
import club.staircrusher.api.spec.dto.RegisterToiletReviewPost200Response
import club.staircrusher.api.spec.dto.RegisterToiletReviewRequestDto
import club.staircrusher.api.spec.dto.ToiletLocationTypeDto
import club.staircrusher.place.application.port.out.accessibility.persistence.toilet_review.ToiletReviewRepository
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RegisterToiletReviewTest : AccessibilityITBase() {
    @Autowired
    private lateinit var toiletReviewRepository: ToiletReviewRepository

    @BeforeEach
    fun setUp() {
        toiletReviewRepository.deleteAll()
    }

    @Test
    fun `화장실 위치가 place 라면 층과 출입문 정보가 필수다`() {
        val user = testDataGenerator.createIdentifiedUser()
        val place = testDataGenerator.createBuildingAndPlace()

        val request = RegisterToiletReviewRequestDto(
            placeId = place.id,
            toiletLocationType = ToiletLocationTypeDto.PLACE,
            floor = 1,
            entranceDoorTypes = listOf(EntranceDoorType.AUTOMATIC),
            imageUrls = listOf("example.com/toilet1.jpg"),
            comment = "화장실이 깨끗하고, 접근성이 좋았습니다.",
        )

        mvc
            .sccRequest("/registerToiletReview", request, user.account)
            .apply {
                val result = getResult(RegisterToiletReviewPost200Response::class)

                Assertions.assertNotNull(result.toiletReview)
                Assertions.assertEquals(result.toiletReview!!.user.id, user.account.id)
                Assertions.assertEquals(1, result.toiletReview!!.images!!.size)
            }

        val failingRequest = RegisterToiletReviewRequestDto(
            placeId = place.id,
            toiletLocationType = ToiletLocationTypeDto.PLACE,
            floor = null,
            entranceDoorTypes = null,
            imageUrls = listOf("example.com/toilet1.jpg"),
            comment = "화장실이 깨끗하고, 접근성이 좋았습니다.",
        )

        mvc
            .sccRequest("/registerToiletReview", failingRequest, user.account)
            .andExpect {
                status { is5xxServerError() }
            }
    }

    @Test
    fun `화장실 위치를 기타 로 선택한 경우 설명이 필수다`() {
        val user = testDataGenerator.createIdentifiedUser()
        val place = testDataGenerator.createBuildingAndPlace()

        val request = RegisterToiletReviewRequestDto(
            placeId = place.id,
            toiletLocationType = ToiletLocationTypeDto.ETC,
            floor = null,
            entranceDoorTypes = null,
            imageUrls = null,
            comment = "화장실이 깨끗하고, 접근성이 좋았습니다.",
        )

        mvc
            .sccRequest("/registerToiletReview", request, user.account)
            .apply {
                val result = getResult(RegisterToiletReviewPost200Response::class)

                Assertions.assertNotNull(result.toiletReview)
                Assertions.assertEquals(result.toiletReview!!.user.id, user.account.id)
            }

        val failingRequest = RegisterToiletReviewRequestDto(
            placeId = place.id,
            toiletLocationType = ToiletLocationTypeDto.ETC,
            floor = null,
            entranceDoorTypes = null,
            imageUrls = null,
            comment = null,
        )

        mvc
            .sccRequest("/registerToiletReview", failingRequest, user.account)
            .andExpect {
                status { is5xxServerError() }
            }
    }
}
