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
        buildingAccessibilityUpvoteRepository.deleteAll()
        buildingAccessibilityRepository.deleteAll()
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
                        hasElevator = true, elevatorStairInfo = StairInfo.OVER_SIX, elevatorStairHeightLevel = null,
                        entranceDoorTypes = listOf(EntranceDoorType.Hinged)
                    )
                },
                // 입구계단2-5칸,경사로O,엘리베이터O,엘리베이터계단1칸,자동미닫이
                testDataGenerator.createBuilding().let { building ->
                    building to getRequestParams(
                        building, entranceStairInfo = StairInfo.TWO_TO_FIVE, entranceStairHeightLevel = null,
                        hasSlope = true,
                        hasElevator = true, elevatorStairInfo = StairInfo.ONE, elevatorStairHeightLevel = StairHeightLevel.OVER_THUMB,
                        entranceDoorTypes = listOf(EntranceDoorType.Automatic, EntranceDoorType.Sliding)
                    )
                },
                // 입구계단1칸,경사로O,엘리베이터X,문없음
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
            val user = transactionManager.doInTransaction { testDataGenerator.createIdentifiedUser() }
            val place = transactionManager.doInTransaction { testDataGenerator.createBuildingAndPlace(placeName = "장소장소") }

            val params = getDefaultRequestParams(place.building)
            mvc.sccRequest("/registerBuildingAccessibility", params, userAccount = user.account)
            mvc
                .sccRequest("/getAccessibility", GetAccessibilityPostRequest(place.id), userAccount = user.account)
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
                    assertEquals(user.account.id, result.buildingAccessibilityComments[0].user!!.id)
                    assertEquals("건물 코멘트", result.buildingAccessibilityComments[0].comment)
                }
        }
    }

    @Test
    fun `240401 이전 버전에서도 정상적으로 등록된다`() {
        val user = transactionManager.doInTransaction { testDataGenerator.createIdentifiedUser() }
        val placesAndParams = transactionManager.doInTransaction {
            listOf(
                // 입구계단X,경사로X,엘리베이터X,엘리베이터계단X
                testDataGenerator.createBuildingAndPlace(placeName = "장소장소").let { place ->
                    place to getRequestParamsBefore2404(
                        place.building,
                        entranceStairInfo = StairInfo.NONE,
                        hasSlope = false,
                        hasElevator = false,
                        elevatorStairInfo = StairInfo.UNDEFINED
                    )
                },
                // 입구계단O,경사로O,엘리베이터X,엘리베이터계단X
                testDataGenerator.createBuildingAndPlace(placeName = "장소장소").let { place ->
                    place to getRequestParamsBefore2404(
                        place.building,
                        entranceStairInfo = StairInfo.TWO_TO_FIVE,
                        hasSlope = true,
                        hasElevator = false,
                        elevatorStairInfo = StairInfo.UNDEFINED
                    )
                },
                // 입구계단X,경사로O,엘리베이터O,엘리베이터계단O
                testDataGenerator.createBuildingAndPlace(placeName = "장소장소").let { place ->
                    place to getRequestParamsBefore2404(
                        place.building,
                        entranceStairInfo = StairInfo.NONE,
                        hasSlope = true,
                        hasElevator = true,
                        elevatorStairInfo = StairInfo.TWO_TO_FIVE
                    )
                },
                // 입구계단O,경사로O,엘리베이터O,엘리베이터계단O
                testDataGenerator.createBuildingAndPlace(placeName = "장소장소").let { place ->
                    place to getRequestParamsBefore2404(
                        place.building,
                        entranceStairInfo = StairInfo.OVER_SIX,
                        hasSlope = true,
                        hasElevator = true,
                        elevatorStairInfo = StairInfo.OVER_SIX
                    )
                },
            )
        }

        placesAndParams.forEachIndexed { index, (place, params) ->
            mvc.sccRequest("/registerBuildingAccessibility", params, userAccount = user.account)
            mvc
                .sccRequest("/getAccessibility", GetAccessibilityPostRequest(place.id), userAccount = user.account)
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
                    assertEquals(user.account.id, result.buildingAccessibilityComments[0].user!!.id)
                    assertEquals("건물 코멘트", result.buildingAccessibilityComments[0].comment)
                }
        }
    }


    @Test
    fun `클라이언트에서 올려준 정보의 정합성이 맞지 않는 경우 에러가 난다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
        }

        val params = getDefaultRequestParams(place.building).copy(
            hasElevator = false,
            elevatorStairInfo = StairInfo.TWO_TO_FIVE.toDTO(), // 엘리베이터가 없는데 계단 정보가 UNDEFINED가 아니다.
        )
        mvc
            .sccRequest("/registerBuildingAccessibility", params, userAccount = user.account)
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
            testDataGenerator.createIdentifiedUser()
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
            .sccRequest("/registerBuildingAccessibility", getDefaultRequestParams(place.building), userAccount = user.account)
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

    private fun getRequestParamsBefore2404(
        building: Building,
        entranceStairInfo: StairInfo,
        hasSlope: Boolean,
        hasElevator: Boolean,
        elevatorStairInfo: StairInfo,
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
