package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toDTO
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toModel
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityPost200Response
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityRequestDto
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.Place
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RegisterPlaceAccessibilityTest : AccessibilityITBase() {
    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        placeAccessibilityRepository.removeAll()
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
    fun `로그인되어 있지 않아도 등록이 잘 된다`() {
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
        }
        mvc
            .sccRequest("/registerPlaceAccessibility", getDefaultRequestParams(place))
            .apply {
                val accessibilityInfo = getResult(RegisterPlaceAccessibilityPost200Response::class).accessibilityInfo
                assertNull(accessibilityInfo.placeAccessibility!!.registeredUserName)
                assertNull(accessibilityInfo.placeAccessibilityComments[0].user)
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
            .sccRequest("/registerPlaceAccessibility", getDefaultRequestParams(place))
            .andExpect {
                status {
                    isBadRequest()
                }
            }
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
