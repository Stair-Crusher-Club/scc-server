package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toDTO
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toModel
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.RegisterAccessibilityPost200Response
import club.staircrusher.api.spec.dto.RegisterAccessibilityPostRequest
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityRequestDto
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityRequestDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.Place
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RegisterAccessibilityTest : AccessibilityITBase() {
    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    private lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository

    @Autowired
    private lateinit var buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository

    @Autowired
    private lateinit var challengeRepository: BuildingAccessibilityRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @Autowired
    private lateinit var challengeContributionRepository: ChallengeContributionRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        placeAccessibilityRepository.removeAll()
        buildingAccessibilityUpvoteRepository.removeAll()
        buildingAccessibilityRepository.removeAll()

        challengeRepository.removeAll()
        challengeParticipationRepository.removeAll()
        challengeContributionRepository.removeAll()
    }

    @Test
    fun testRegisterAccessibility() {
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
                .sccRequest("/registerAccessibility", params, user = user)
                .apply {
                    val result = getResult(RegisterAccessibilityPost200Response::class)
                    val buildingAccessibility = result.buildingAccessibility!!
                    assertEquals(place.building.id, buildingAccessibility.buildingId)
                    assertEquals(StairInfo.NONE, buildingAccessibility.entranceStairInfo.toModel())
                    assertEquals(1, buildingAccessibility.entranceImageUrls.size)
                    assertEquals("buildingAccessibilityEntranceImage", buildingAccessibility.entranceImageUrls[0])
                    assertTrue(buildingAccessibility.hasSlope)
                    assertTrue(buildingAccessibility.hasElevator)
                    assertEquals(StairInfo.TWO_TO_FIVE, buildingAccessibility.elevatorStairInfo.toModel())
                    assertEquals(2, buildingAccessibility.elevatorImageUrls.size)
                    assertEquals("buildingAccessibilityElevatorImage1", buildingAccessibility.elevatorImageUrls[0])
                    assertEquals("buildingAccessibilityElevatorImage2", buildingAccessibility.elevatorImageUrls[1])
                    assertFalse(buildingAccessibility.isUpvoted)
                    assertEquals(0, buildingAccessibility.totalUpvoteCount)

                    assertEquals(1, result.buildingAccessibilityComments.size)
                    assertEquals(place.building.id, result.buildingAccessibilityComments[0].buildingId)
                    assertEquals(user.id, result.buildingAccessibilityComments[0].user!!.id)
                    assertEquals("건물 코멘트", result.buildingAccessibilityComments[0].comment)

                    val placeAccessibility = result.placeAccessibility
                    assertEquals(place.id, placeAccessibility.placeId)
                    assertFalse(placeAccessibility.isFirstFloor)
                    assertEquals(StairInfo.ONE, placeAccessibility.stairInfo.toModel())
                    assertTrue(placeAccessibility.hasSlope)
                    assertTrue(placeAccessibility.imageUrls.isEmpty())

                    assertEquals(1, result.placeAccessibilityComments.size)
                    assertEquals(place.id, result.placeAccessibilityComments[0].placeId)
                    assertEquals(user.id, result.placeAccessibilityComments[0].user!!.id)
                    assertEquals("장소 코멘트", result.placeAccessibilityComments[0].comment)

                    assertEquals(expectedRegisteredUserOrder, result.registeredUserOrder)
                }
        }
    }

    @Test
    fun `클라이언트에서 올려준 정보의 정합성이 맞지 않는 경우 에러가 난다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
        }

        val params1 = getDefaultRequestParams(place).let {
            it.copy(
                buildingAccessibilityParams = it.buildingAccessibilityParams!!.copy(
                    hasElevator = false,
                    elevatorStairInfo = StairInfo.TWO_TO_FIVE.toDTO(), // 엘리베이터가 없는데 계단 정보가 UNDEFINED가 아니다.
                )
            )
        }
        mvc
            .sccRequest("/registerAccessibility", params1, user = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }

        val params2 = getDefaultRequestParams(place).let {
            it.copy(
                buildingAccessibilityParams = it.buildingAccessibilityParams!!.copy(
                    hasElevator = true,
                    elevatorStairInfo = StairInfo.UNDEFINED.toDTO(), // 엘리베이터가 있는데 계단 정보가 UNDEFINED이다.
                )
            )
        }
        mvc
            .sccRequest("/registerAccessibility", params2, user = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }

    @Test
    fun `로그인되어 있지 않으면 등록이 안 된다`() {
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
        }
        mvc
            .sccRequest("/registerAccessibility", getDefaultRequestParams(place))
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `서울, 성남외의 지역을 등록하려면 에러가 난다`() {
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

        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        mvc
            .sccRequest("/registerAccessibility", getDefaultRequestParams(place), user = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }

    @Test
    fun `images 필드가 추가된 이후에는 imageUrls 필드와 images 필드가 모두 채워진다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
        }

        val params = getDefaultRequestParams(place)
        mvc
            .sccRequest("/registerAccessibility", params, user = user)
            .apply {
                val result = getResult(RegisterAccessibilityPost200Response::class)

                val buildingAccessibility = result.buildingAccessibility!!

                assertEquals(1, buildingAccessibility.entranceImageUrls.size)
                assertNotNull(buildingAccessibility.entranceImages)
                assertEquals(1, buildingAccessibility.entranceImages!!.size)

                assertEquals(2, buildingAccessibility.elevatorImageUrls.size)
                assertNotNull(buildingAccessibility.elevatorImages)
                assertEquals(2, buildingAccessibility.elevatorImages!!.size)
            }
    }

    private fun getDefaultRequestParams(place: Place): RegisterAccessibilityPostRequest {
        return RegisterAccessibilityPostRequest(
            buildingAccessibilityParams = RegisterBuildingAccessibilityRequestDto(
                buildingId = place.building.id,
                entranceStairInfo = StairInfo.NONE.toDTO(),
                entranceImageUrls = listOf("buildingAccessibilityEntranceImage"),
                hasSlope = true,
                hasElevator = true,
                elevatorStairInfo = StairInfo.TWO_TO_FIVE.toDTO(),
                elevatorImageUrls = listOf(
                    "buildingAccessibilityElevatorImage1",
                    "buildingAccessibilityElevatorImage2",
                ),
                comment = "건물 코멘트",
            ),
            placeAccessibilityParams = RegisterPlaceAccessibilityRequestDto(
                placeId = place.id,
                isFirstFloor = false,
                stairInfo = StairInfo.ONE.toDTO(),
                imageUrls = emptyList(),
                hasSlope = true,
                comment = "장소 코멘트",
            ),
        )
    }

    private fun getDefaultRequestParamsWithoutBuildingAccessibility(place: Place): RegisterAccessibilityPostRequest {
        return RegisterAccessibilityPostRequest(
            buildingAccessibilityParams = null,
            placeAccessibilityParams = RegisterPlaceAccessibilityRequestDto(
                placeId = place.id,
                isFirstFloor = false,
                stairInfo = StairInfo.ONE.toDTO(),
                imageUrls = emptyList(),
                hasSlope = true,
                comment = "장소 코멘트",
            ),
        )
    }
}
