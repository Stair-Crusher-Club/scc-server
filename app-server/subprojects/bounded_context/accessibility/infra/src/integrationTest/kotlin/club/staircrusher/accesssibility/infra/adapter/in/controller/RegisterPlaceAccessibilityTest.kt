package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toDTO
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toModel
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityPost200Response
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityRequestDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeActionCondition
import club.staircrusher.challenge.domain.model.ChallengeAddressCondition
import club.staircrusher.challenge.domain.model.ChallengeCondition
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.Place
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Clock

class RegisterPlaceAccessibilityTest : AccessibilityITBase() {
    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    private lateinit var challengeRepository: BuildingAccessibilityRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @Autowired
    private lateinit var challengeContributionRepository: ChallengeContributionRepository

    @Autowired
    private lateinit var clock: Clock

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        placeAccessibilityRepository.removeAll()

        challengeRepository.removeAll()
        challengeParticipationRepository.removeAll()
        challengeContributionRepository.removeAll()
    }

    @Test
    fun testRegisterPlaceAccessibility() {
        repeat(3) { idx ->
            val expectedRegisteredUserOrder = idx + 1
            val user = transactionManager.doInTransaction {
                testDataGenerator.createUser()
            }
            val place = transactionManager.doInTransaction {
                testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
            }

            val params = getDefaultRequestParams(place)
            mvc
                .sccRequest("/registerPlaceAccessibility", params, user = user)
                .apply {
                    val result = getResult(RegisterPlaceAccessibilityPost200Response::class)
                    val accessibilityInfo = result.accessibilityInfo
                    assertNull(accessibilityInfo.buildingAccessibility)
                    assertTrue(accessibilityInfo.buildingAccessibilityComments.isEmpty())

                    val placeAccessibility = accessibilityInfo.placeAccessibility!!
                    assertEquals(place.id, placeAccessibility.placeId)
                    assertFalse(placeAccessibility.isFirstFloor)
                    assertEquals(StairInfo.ONE, placeAccessibility.stairInfo.toModel())
                    assertTrue(placeAccessibility.hasSlope)
                    assertTrue(placeAccessibility.imageUrls.isEmpty())

                    val placeAccessibilityComments = accessibilityInfo.placeAccessibilityComments
                    assertEquals(1, placeAccessibilityComments.size)
                    assertEquals(place.id, placeAccessibilityComments[0].placeId)
                    assertEquals(user.id, placeAccessibilityComments[0].user!!.id)
                    assertEquals("장소 코멘트", placeAccessibilityComments[0].comment)

                    assertEquals(expectedRegisteredUserOrder, result.registeredUserOrder)
                }
        }
    }

    @Test
    fun `로그인되어 있지 않으면 등록이 안 된다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
        }
        mvc
            .sccRequest("/registerPlaceAccessibility", getDefaultRequestParams(place))
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `서울, 성남외의 지역을 등록하려면 에러가 난다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(
                placeName = "장소장소",
                buildingAddress = BuildingAddress(
                    siDo = "경기도",
                    siGunGu = "수원시",
                    eupMyeonDong = "영통동",
                    li = "",
                    roadName = "봉영로",
                    mainBuildingNumber = "83",
                    subBuildingNumber = "21",
                ),
            )
        }
        mvc
            .sccRequest("/registerPlaceAccessibility", getDefaultRequestParams(place), user = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }

    @Test
    fun `참여하고 있는 챌린지 조건이 맞으면 기여도를 올린다`() {
        val (user, place, challenge) = transactionManager.doInTransaction {
            return@doInTransaction Triple(
                testDataGenerator.createUser(),
                testDataGenerator.createBuildingAndPlace(
                    placeName = "밀크빌딩",
                    buildingAddress = BuildingAddress(
                        siDo = "서울특별시",
                        siGunGu = "성동구",
                        eupMyeonDong = "성수동",
                        li = "",
                        roadName = "성수일로4길",
                        mainBuildingNumber = "4",
                        subBuildingNumber = ""
                    ),
                ),
                testDataGenerator.createChallenge(
                    name = "참여하는 챌린지",
                    isComplete = false,
                    startsAt = Challenge.MIN_TIME.plusSeconds(60),
                    endsAt = Challenge.MAX_TIME.minusSeconds(60),
                    goal = 1,
                    conditions = listOf(
                        ChallengeCondition(
                            addressCondition = ChallengeAddressCondition(rawEupMyeonDongs = listOf("성수동")),
                            actionCondition = ChallengeActionCondition(types = listOf(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY))
                        )
                    ),
                )
            )
        }
        transactionManager.doInTransaction { testDataGenerator.participateChallenge(user, challenge, clock.instant()) }
        mvc
            .sccRequest(
                "/registerPlaceAccessibility",
                getDefaultRequestParams(place),
                user = user
            )
            .andExpect { status { is2xxSuccessful() } }
        val contributions = challengeContributionRepository.findByUserId(user.id)
        assertTrue(contributions.count() == 1)
        assertTrue(contributions.firstOrNull()?.challengeId == challenge.id)
    }

    @Test
    fun `참여하고 있는 챌린지 조건이 맞지 않으면 기여도를 올리지 않는다`() {
        val user = transactionManager.doInTransaction { testDataGenerator.createUser() }
        val (challenge1, challenge2, challenge3) = transactionManager.doInTransaction {
            return@doInTransaction Triple(
                testDataGenerator.createChallenge(
                    name = "송파동 장소챌린지",
                    isComplete = false,
                    startsAt = Challenge.MIN_TIME.plusSeconds(60),
                    endsAt = Challenge.MAX_TIME.minusSeconds(60),
                    goal = 1,
                    conditions = listOf(
                        ChallengeCondition(
                            addressCondition = ChallengeAddressCondition(rawEupMyeonDongs = listOf("송파동")),
                            actionCondition = ChallengeActionCondition(types = listOf(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY))
                        )
                    ),
                ),
                testDataGenerator.createChallenge(
                    name = "성수동 빌딩챌린지",
                    isComplete = false,
                    startsAt = Challenge.MIN_TIME.plusSeconds(60),
                    endsAt = Challenge.MAX_TIME.minusSeconds(60),
                    goal = 1,
                    conditions = listOf(
                        ChallengeCondition(
                            addressCondition = ChallengeAddressCondition(rawEupMyeonDongs = listOf("성수동")),
                            actionCondition = ChallengeActionCondition(types = listOf(ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY))
                        )
                    ),
                ),
                testDataGenerator.createChallenge(
                    name = "오픈전 성수동 챌린지",
                    isComplete = false,
                    startsAt = clock.instant().plusSeconds(60),
                    endsAt = Challenge.MAX_TIME.minusSeconds(60),
                    goal = 1,
                    conditions = listOf(
                        ChallengeCondition(
                            addressCondition = ChallengeAddressCondition(rawEupMyeonDongs = listOf("성수동")),
                            actionCondition = ChallengeActionCondition(types = listOf(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY))
                        )
                    ),
                )
            )
        }
        transactionManager.doInTransaction {
            testDataGenerator.participateChallenge(user, challenge1, clock.instant())
            testDataGenerator.participateChallenge(user, challenge2, clock.instant())
            testDataGenerator.participateChallenge(user, challenge3, clock.instant())
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(
                placeName = "테스트장소",
                buildingAddress = BuildingAddress(
                    siDo = "서울특별시",
                    siGunGu = "성동구",
                    eupMyeonDong = "성수동",
                    li = "",
                    roadName = "성수일로4길",
                    mainBuildingNumber = "4",
                    subBuildingNumber = ""
                ),
            )
        }
        mvc
            .sccRequest(
                "/registerPlaceAccessibility",
                getDefaultRequestParams(place),
                user = user
            )
            .andExpect { status { is2xxSuccessful() } }
        val participations = challengeParticipationRepository.findByUserId(user.id)
        assertTrue(participations.count() == 3)
        val contributions = challengeContributionRepository.findByUserId(user.id)
        assertTrue(contributions.isEmpty())
    }

    @Test
    fun `참여하고 있는 않은 챌린지 조건이 맞아도 기여도를 올리지 않는다`() {
        val (user, place, _) = transactionManager.doInTransaction {
            return@doInTransaction Triple(
                testDataGenerator.createUser(),
                testDataGenerator.createBuildingAndPlace(
                    placeName = "밀크빌딩",
                    buildingAddress = BuildingAddress(
                        siDo = "서울특별시",
                        siGunGu = "성동구",
                        eupMyeonDong = "성수동",
                        li = "",
                        roadName = "성수일로4길",
                        mainBuildingNumber = "4",
                        subBuildingNumber = ""
                    ),
                ),
                testDataGenerator.createChallenge(
                    name = "참여 안하는 챌린지",
                    isComplete = false,
                    startsAt = Challenge.MIN_TIME.plusSeconds(60),
                    endsAt = Challenge.MAX_TIME.minusSeconds(60),
                    goal = 1,
                    conditions = listOf(
                        ChallengeCondition(
                            addressCondition = ChallengeAddressCondition(rawEupMyeonDongs = listOf("성수동")),
                            actionCondition = ChallengeActionCondition(types = listOf(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY))
                        )
                    )
                )
            )
        }
        mvc
            .sccRequest(
                "/registerPlaceAccessibility",
                getDefaultRequestParams(place),
                user = user
            )
            .andExpect { status { is2xxSuccessful() } }
        val participations = challengeParticipationRepository.findByUserId(user.id)
        assertTrue(participations.isEmpty())
        val contributions = challengeContributionRepository.findByUserId(user.id)
        assertTrue(contributions.isEmpty())
    }

    private fun getDefaultRequestParams(place: Place): RegisterPlaceAccessibilityRequestDto {
        return RegisterPlaceAccessibilityRequestDto(
            placeId = place.id,
            isFirstFloor = false,
            stairInfo = StairInfo.ONE.toDTO(),
            imageUrls = emptyList(),
            hasSlope = true,
            comment = "장소 코멘트",
        )
    }
}
