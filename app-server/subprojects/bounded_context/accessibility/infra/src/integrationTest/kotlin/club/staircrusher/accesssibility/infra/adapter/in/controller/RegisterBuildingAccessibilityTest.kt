package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.domain.model.EntranceDoorType
import club.staircrusher.accessibility.domain.model.StairHeightLevel
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toDTO
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.AccessibilityInfoDto
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityRequestDto
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.BuildingAddress
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
    fun `정상적으로 등록된다`() {
        val buildingsAndParams = transactionManager.doInTransaction {
            listOf(
                // 입구계단X,경사로X,엘리베이터O,엘리베이터계단X,회전문
                testDataGenerator.createBuilding().let { building ->
                    building to getRequestParams(
                        building, entranceStairInfo = StairInfo.NONE, entranceStairHeightLevel = null,
                        hasSlope = false,
                        hasElevator = true, elevatorStairInfo = StairInfo.NONE, elevatorStairHeightLevel = null,
                        entranceDoorTypes = listOf(EntranceDoorType.Revolving)
                    )
                },
                // 입구계단X,경사로O,엘리베이터O,엘리베이터계단O,여닫이
                testDataGenerator.createBuilding().let { building ->
                    building to getRequestParams(
                        building, entranceStairInfo = StairInfo.NONE, entranceStairHeightLevel = null,
                        hasSlope = true,
                        hasElevator = true, elevatorStairInfo = StairInfo.OVER_SIX, elevatorStairHeightLevel = StairHeightLevel.HALF_THUMB,
                        entranceDoorTypes = listOf(EntranceDoorType.Hinged)
                    )
                },
                // 입구계단O,경사로O,엘리베이터O,엘리베이터계단O,자동미닫이
                testDataGenerator.createBuilding().let { building ->
                    building to getRequestParams(
                        building, entranceStairInfo = StairInfo.TWO_TO_FIVE, entranceStairHeightLevel = StairHeightLevel.HALF_THUMB,
                        hasSlope = true,
                        hasElevator = true, elevatorStairInfo = StairInfo.OVER_SIX, elevatorStairHeightLevel = StairHeightLevel.OVER_THUMB,
                        entranceDoorTypes = listOf(EntranceDoorType.Automatic, EntranceDoorType.Sliding)
                    )
                },
                // 입구계단O,경사로O,엘리베이터X,문없음
                testDataGenerator.createBuilding().let { building ->
                    building to getRequestParams(
                        building, entranceStairInfo = StairInfo.ONE, entranceStairHeightLevel = StairHeightLevel.THUMB,
                        hasSlope = true,
                        hasElevator = false, elevatorStairInfo = StairInfo.NONE, elevatorStairHeightLevel = null,
                        entranceDoorTypes = listOf(EntranceDoorType.None)
                    )
                },
            )
        }
        buildingsAndParams.forEachIndexed { idx, (building, params) ->
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
                    assertEquals(params.entranceStairInfo, buildingAccessibility.entranceStairInfo)
                    assertEquals(params.entranceStairHeightLevel, buildingAccessibility.entranceStairHeightLevel)
                    assertEquals(params.entranceImageUrls.size, buildingAccessibility.entranceImageUrls.size)
                    assertEquals(params.entranceImageUrls[0], buildingAccessibility.entranceImageUrls[0])
                    assertEquals(params.hasSlope, buildingAccessibility.hasSlope)
                    assertEquals(params.hasElevator, buildingAccessibility.hasElevator)
                    assertArrayEquals(params.entranceDoorTypes?.toTypedArray(), buildingAccessibility.entranceDoorTypes?.toTypedArray())
                    assertEquals(params.elevatorStairInfo, buildingAccessibility.elevatorStairInfo)
                    assertEquals(params.elevatorStairHeightLevel, buildingAccessibility.elevatorStairHeightLevel)
                    assertEquals(2, buildingAccessibility.elevatorImageUrls.size)
                    assertEquals(params.elevatorImageUrls[0], buildingAccessibility.elevatorImageUrls[0])
                    assertEquals(params.elevatorImageUrls[1], buildingAccessibility.elevatorImageUrls[1])
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
    fun `240401 이전 버전에서도 정상적으로 등록된다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
        }
        val params = getDefaultRequestParamsBefore2404(place.building)
        mvc.sccRequest("/registerBuildingAccessibility", params, user = user)
        mvc
            .sccRequest("/getAccessibility", GetAccessibilityPostRequest(place.id), user = user)
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                val buildingAccessibility = result.buildingAccessibility!!
                assertEquals(place.building.id, buildingAccessibility.buildingId)
                assertEquals(params.entranceStairInfo, buildingAccessibility.entranceStairInfo)
                assertEquals(params.entranceStairHeightLevel, null)
                assertEquals(params.entranceImageUrls.size, buildingAccessibility.entranceImageUrls.size)
                assertEquals(params.entranceImageUrls[0], buildingAccessibility.entranceImageUrls[0])
                assertEquals(params.hasSlope, buildingAccessibility.hasSlope)
                assertEquals(params.hasElevator, buildingAccessibility.hasElevator)
                assertEquals(params.entranceDoorTypes, null)
                assertEquals(params.elevatorStairInfo, buildingAccessibility.elevatorStairInfo)
                assertEquals(params.elevatorStairHeightLevel, null)
                assertEquals(2, buildingAccessibility.elevatorImageUrls.size)
                assertEquals(params.elevatorImageUrls[0], buildingAccessibility.elevatorImageUrls[0])
                assertEquals(params.elevatorImageUrls[1], buildingAccessibility.elevatorImageUrls[1])
                assertFalse(buildingAccessibility.isUpvoted)
                assertEquals(0, buildingAccessibility.totalUpvoteCount)

                assertEquals(1, result.buildingAccessibilityComments.size)
                assertEquals(place.building.id, result.buildingAccessibilityComments[0].buildingId)
                assertEquals(user.id, result.buildingAccessibilityComments[0].user!!.id)
                assertEquals("건물 코멘트", result.buildingAccessibilityComments[0].comment)
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
    fun `로그인되어 있지 않으면 등록이 안 된다`() {
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
        }
        mvc.sccRequest("/registerBuildingAccessibility", getDefaultRequestParams(place.building))
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
            .sccRequest("/registerBuildingAccessibility", getDefaultRequestParams(place.building), user = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }

    private fun getDefaultRequestParams(
        building: Building,
    ): RegisterBuildingAccessibilityRequestDto {
        return getRequestParams(
            building = building,
            entranceStairInfo = StairInfo.TWO_TO_FIVE,
            entranceStairHeightLevel = StairHeightLevel.OVER_THUMB,
            hasSlope = true,
            hasElevator = true,
            elevatorStairInfo = StairInfo.OVER_SIX,
            elevatorStairHeightLevel = StairHeightLevel.HALF_THUMB,
            entranceDoorTypes = listOf(EntranceDoorType.Automatic, EntranceDoorType.Sliding),
        )
    }

    private fun getRequestParams(
        building: Building,
        entranceStairInfo: StairInfo,
        entranceStairHeightLevel: StairHeightLevel?,
        hasSlope: Boolean,
        hasElevator: Boolean,
        elevatorStairInfo: StairInfo,
        elevatorStairHeightLevel: StairHeightLevel?,
        entranceDoorTypes: List<EntranceDoorType>
    ): RegisterBuildingAccessibilityRequestDto {
        return RegisterBuildingAccessibilityRequestDto(
            buildingId = building.id,
            entranceStairInfo = entranceStairInfo.toDTO(),
            entranceStairHeightLevel = entranceStairHeightLevel?.toDTO(),
            entranceImageUrls = listOf("buildingAccessibilityEntranceImage"),
            hasSlope = hasSlope,
            hasElevator = hasElevator,
            entranceDoorTypes = entranceDoorTypes.map { it.toDTO() },
            elevatorStairInfo = elevatorStairInfo.toDTO(),
            elevatorStairHeightLevel = elevatorStairHeightLevel?.toDTO(),
            elevatorImageUrls = listOf(
                "buildingAccessibilityElevatorImage1",
                "buildingAccessibilityElevatorImage2",
            ),
            comment = "건물 코멘트",
        )
    }

    private fun getDefaultRequestParamsBefore2404(
        building: Building,
        entranceStairInfo: StairInfo = StairInfo.ONE,
        hasSlope: Boolean = true,
        hasElevator: Boolean = true,
        elevatorStairInfo: StairInfo = StairInfo.TWO_TO_FIVE,
    ): RegisterBuildingAccessibilityRequestDto {
        return RegisterBuildingAccessibilityRequestDto(
            buildingId = building.id,
            entranceStairInfo = entranceStairInfo.toDTO(),
            entranceStairHeightLevel = null,
            entranceImageUrls = listOf("buildingAccessibilityEntranceImage"),
            entranceDoorTypes = null,
            hasSlope = hasSlope,
            hasElevator = hasElevator,
            elevatorStairInfo = elevatorStairInfo.toDTO(),
            elevatorStairHeightLevel = null,
            elevatorImageUrls = listOf(
                "buildingAccessibilityElevatorImage1",
                "buildingAccessibilityElevatorImage2",
            ),
            comment = "건물 코멘트",
        )
    }

}
