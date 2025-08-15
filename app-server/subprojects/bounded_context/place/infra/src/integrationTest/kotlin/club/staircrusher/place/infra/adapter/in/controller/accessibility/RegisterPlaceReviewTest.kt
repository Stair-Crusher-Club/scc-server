package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.RecommendedMobilityTypeDto
import club.staircrusher.api.spec.dto.RegisterPlaceReviewPost200Response
import club.staircrusher.api.spec.dto.RegisterPlaceReviewRequestDto
import club.staircrusher.api.spec.dto.SpaciousTypeDto
import club.staircrusher.api.spec.dto.UserMobilityToolDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeActionCondition
import club.staircrusher.challenge.domain.model.ChallengeAddressCondition
import club.staircrusher.challenge.domain.model.ChallengeCondition
import club.staircrusher.place.application.port.out.accessibility.persistence.place_review.PlaceReviewRepository
import club.staircrusher.place.domain.model.place.BuildingAddress
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import club.staircrusher.stdlib.clock.SccClock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RegisterPlaceReviewTest : AccessibilityITBase() {
    @Autowired
    private lateinit var placeReviewRepository: PlaceReviewRepository

    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @Autowired
    private lateinit var challengeContributionRepository: ChallengeContributionRepository

    @BeforeEach
    fun setUp() {
        placeReviewRepository.deleteAll()
        
        challengeRepository.deleteAll()
        challengeParticipationRepository.deleteAll()
        challengeContributionRepository.deleteAll()
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

    @Test
    fun `참여하고 있는 챌린지 조건이 맞으면 리뷰 등록 시 기여도를 올린다`() {
        val (user, place, challenge) = transactionManager.doInTransaction {
            val user = testDataGenerator.createIdentifiedUser()
            val place = testDataGenerator.createBuildingAndPlace(
                placeName = "성수동맛집",
                buildingAddress = BuildingAddress(
                    siDo = "서울특별시",
                    siGunGu = "성동구",
                    eupMyeonDong = "성수동1가",
                    li = "",
                    roadName = "성수일로4길",
                    mainBuildingNumber = "4",
                    subBuildingNumber = ""
                ),
            )
            val challenge = testDataGenerator.createChallenge(
                name = "성수동 리뷰 챌린지",
                isComplete = false,
                startsAt = Challenge.MIN_TIME.plusSeconds(60),
                endsAt = Challenge.MAX_TIME.minusSeconds(60),
                goal = 1,
                conditions = listOf(
                    ChallengeCondition(
                        addressCondition = ChallengeAddressCondition(rawEupMyeonDongs = listOf("성수동", "테스트동")),
                        actionCondition = ChallengeActionCondition(types = listOf(ChallengeActionCondition.Type.PLACE_REVIEW))
                    )
                ),
            )
            Triple(user, place, challenge)
        }

        transactionManager.doInTransaction {
            testDataGenerator.participateChallenge(
                user.account,
                challenge,
                SccClock.instant()
            )
        }

        val request = RegisterPlaceReviewRequestDto(
            placeId = place.id,
            recommendedMobilityTypes = listOf(RecommendedMobilityTypeDto.MANUAL_WHEELCHAIR),
            spaciousType = SpaciousTypeDto.TIGHT,
            mobilityTool = UserMobilityToolDto.MANUAL_WHEELCHAIR,
            seatTypes = listOf("좌식"),
            orderMethods = listOf("카운터에서 주문"),
            imageUrls = emptyList(),
            comment = "접근성이 좋았습니다.",
            features = listOf("접근성 좋음"),
        )

        mvc
            .sccRequest("/registerPlaceReview", request, user.account)
            .andExpect { status { is2xxSuccessful() } }

        val contributions = challengeContributionRepository.findByUserId(user.account.id)
        Assertions.assertTrue(contributions.count() == 1)
        Assertions.assertTrue(contributions.firstOrNull()?.challengeId == challenge.id)
    }
}
