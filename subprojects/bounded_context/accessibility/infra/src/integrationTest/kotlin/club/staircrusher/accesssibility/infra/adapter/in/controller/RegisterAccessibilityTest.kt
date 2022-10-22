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
import club.staircrusher.api.spec.dto.RegisterAccessibilityPostRequestBuildingAccessibilityParams
import club.staircrusher.api.spec.dto.RegisterAccessibilityPostRequestPlaceAccessibilityParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        placeAccessibilityRepository.removeAll()
        buildingAccessibilityUpvoteRepository.removeAll()
        buildingAccessibilityRepository.removeAll()
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

            val params = RegisterAccessibilityPostRequest(
                buildingAccessibilityParams = RegisterAccessibilityPostRequestBuildingAccessibilityParams(
                    buildingId = place.building!!.id,
                    entranceStairInfo = StairInfo.NONE.toDTO(),
                    hasSlope = true,
                    hasElevator = true,
                    elevatorStairInfo = StairInfo.TWO_TO_FIVE.toDTO(),
                    comment = "건물 코멘트",
                ),
                placeAccessibilityParams = RegisterAccessibilityPostRequestPlaceAccessibilityParams(
                    placeId = place.id,
                    isFirstFloor = false,
                    stairInfo = StairInfo.ONE.toDTO(),
                    hasSlope = true,
                    comment = "장소 코멘트",
                ),
            )
            mvc
                .sccRequest("/registerAccessibility", params, user = user)
                .apply {
                    val result = getResult(RegisterAccessibilityPost200Response::class)
                    val buildingAccessibility = result.buildingAccessibility!!
                    assertEquals(place.building!!.id, buildingAccessibility.buildingId)
                    assertEquals(StairInfo.NONE, buildingAccessibility.entranceStairInfo.toModel())
                    assertTrue(buildingAccessibility.hasSlope)
                    assertTrue(buildingAccessibility.hasElevator)
                    assertEquals(StairInfo.TWO_TO_FIVE, buildingAccessibility.elevatorStairInfo.toModel())
                    assertFalse(buildingAccessibility.isUpvoted)
                    assertEquals(0, buildingAccessibility.totalUpvoteCount)

                    assertEquals(1, result.buildingAccessibilityComments.size)
                    assertEquals(place.building!!.id, result.buildingAccessibilityComments[0].buildingId)
                    assertEquals(user.id, result.buildingAccessibilityComments[0].user!!.id)
                    assertEquals("건물 코멘트", result.buildingAccessibilityComments[0].comment)

                    val placeAccessibility = result.placeAccessibility
                    assertEquals(place.id, placeAccessibility.placeId)
                    assertFalse(placeAccessibility.isFirstFloor)
                    assertEquals(StairInfo.ONE, placeAccessibility.stairInfo.toModel())
                    assertTrue(placeAccessibility.hasSlope)

                    assertEquals(1, result.placeAccessibilityComments.size)
                    assertEquals(place.id, result.placeAccessibilityComments[0].placeId)
                    assertEquals(user.id, result.placeAccessibilityComments[0].user!!.id)
                    assertEquals("장소 코멘트", result.placeAccessibilityComments[0].comment)

                    // TODO: n번째 정복자 올바르게 채워주기
//                    assertEquals(expectedRegisteredUserOrder, result.registeredUserOrder)
                }
        }
    }

    // TODO: 유저 없는 경우도 테스트?
}
