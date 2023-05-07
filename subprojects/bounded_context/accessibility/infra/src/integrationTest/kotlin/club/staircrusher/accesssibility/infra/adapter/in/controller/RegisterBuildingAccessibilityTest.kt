package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toDTO
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toModel
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.AccessibilityInfoDto
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityRequestDto
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.BuildingAddress
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RegisterBuildingAccessibilityTest : AccessibilityITBase() {
    @Autowired
    private lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository
    @Autowired
    private lateinit var buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        buildingAccessibilityUpvoteRepository.removeAll()
        buildingAccessibilityRepository.removeAll()
    }

    @Test
    fun testRegisterBuildingAccessibility() {
        repeat(3) { idx ->
            val expectedRegisteredUserOrder = idx + 1
            val user = transactionManager.doInTransaction {
                testDataGenerator.createUser()
            }
            val place = transactionManager.doInTransaction {
                testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
            }

            val params = getDefaultRequestParams(place.building)
            mvc.sccRequest("/registerBuildingAccessibility", params, user = user)
            mvc
                .sccRequest("/getAccessibility", GetAccessibilityPostRequest(place.id), user = user)
                .apply {
                    val result = getResult(AccessibilityInfoDto::class)
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

        val params1 = getDefaultRequestParams(place.building).copy(
            hasElevator = false,
            elevatorStairInfo = StairInfo.TWO_TO_FIVE.toDTO(), // 엘리베이터가 없는데 계단 정보가 UNDEFINED가 아니다.
        )
        mvc
            .sccRequest("/registerBuildingAccessibility", params1, user = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }

        val params2 = getDefaultRequestParams(place.building).copy(
            hasElevator = true,
            elevatorStairInfo = StairInfo.UNDEFINED.toDTO(), // 엘리베이터가 있는데 계단 정보가 UNDEFINED이다.
        )
        mvc
            .sccRequest("/registerAccessibility", params2, user = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }

    @Test
    fun `로그인되어 있지 않아도 등록이 잘 된다`() {
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
        }
        mvc.sccRequest("/registerBuildingAccessibility", getDefaultRequestParams(place.building))
        mvc
            .sccRequest("/getAccessibility", GetAccessibilityPostRequest(place.id))
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertNull(result.buildingAccessibility!!.registeredUserName)
                assertNull(result.buildingAccessibilityComments[0].user)
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
        mvc
            .sccRequest("/registerBuildingAccessibility", getDefaultRequestParams(place.building))
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }

    private fun getDefaultRequestParams(building: Building): RegisterBuildingAccessibilityRequestDto {
        return RegisterBuildingAccessibilityRequestDto(
            buildingId = building.id,
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
        )
    }
}
