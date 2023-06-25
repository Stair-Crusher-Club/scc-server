package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.AccessibilityInfoDto
import club.staircrusher.api.spec.dto.DeleteAccessibilityPostRequest
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.testing.spring_it.mock.MockSccClock
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class DeleteAccessibilityTest : AccessibilityITBase() {
    @Autowired
    lateinit var mockSccClock: MockSccClock

    @Autowired
    lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository

    @Test
    fun `전반적인 테스트`() {
        // given: 한 건물에 두 개의 장소 정보를 등록한다.
        val (user, place1, placeAccessibility1, buildingAccessibility) = registerAccessibility()
        val building = place1.building
        val (_, place2, placeAccessibility2) = registerAccessibility(overridingUser = user, overridingBuilding = building)

        // when: 첫 번째 장소 정보를 삭제한다.
        val deleteAccessibilityParams1 = DeleteAccessibilityPostRequest(placeAccessibilityId = placeAccessibility1.id)
        mvc
            .sccRequest("/deleteAccessibility", deleteAccessibilityParams1, user = user)
            .apply {
                transactionManager.doInTransaction {
                    assertNull(placeAccessibilityRepository.findByIdOrNull(placeAccessibility1.id))
                    assertNotNull(buildingAccessibilityRepository.findByIdOrNull(buildingAccessibility.id))
                }
            }

        val getAccessibilityParams1 = GetAccessibilityPostRequest(placeId = place1.id)
        mvc
            .sccRequest("/getAccessibility", getAccessibilityParams1)
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertNull(result.placeAccessibility)
                assertTrue(result.placeAccessibilityComments.isEmpty())
                assertNotNull(result.buildingAccessibility)
                assertTrue(result.buildingAccessibilityComments.isNotEmpty())
            }

        // when: 마지막 남은 장소 정보를 삭제한다
        val deleteAccessibilityParams2 = DeleteAccessibilityPostRequest(placeAccessibilityId = placeAccessibility2.id)
        mvc
            .sccRequest("/deleteAccessibility", deleteAccessibilityParams2, user = user)
            .apply {
                transactionManager.doInTransaction {
                    assertNull(placeAccessibilityRepository.findByIdOrNull(placeAccessibility2.id))
                    // 건물의 마지막 장소 정보가 삭제되면 건물 정보도 삭제된다.
                    assertNull(buildingAccessibilityRepository.findByIdOrNull(buildingAccessibility.id))
                }
            }

        val getAccessibilityParams2 = GetAccessibilityPostRequest(placeId = place1.id)
        mvc
            .sccRequest("/getAccessibility", getAccessibilityParams2)
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertNull(result.placeAccessibility)
                assertTrue(result.placeAccessibilityComments.isEmpty())
                assertNull(result.buildingAccessibility)
                assertTrue(result.buildingAccessibilityComments.isEmpty())
            }
    }

    @Test
    fun `본인이 등록하지 않은 정보는 삭제할 수 없다`() {
        val placeAccessibility = registerAccessibility().placeAccessibility
        val otherUser = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val params = DeleteAccessibilityPostRequest(placeAccessibilityId = placeAccessibility.id)
        mvc
            .sccRequest("/deleteAccessibility", params, user = otherUser)
            .andExpect {
                status { isBadRequest() }
            }

        mvc
            .sccRequest("/deleteAccessibility", params)
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `등록한지 6시간이 지난 장소 정보는 삭제할 수 없다`() {
        val (user, _, placeAccessibility) = registerAccessibility()
        mockSccClock.advanceTime(PlaceAccessibility.deletableDuration + Duration.ofMinutes(1))

        val params = DeleteAccessibilityPostRequest(placeAccessibilityId = placeAccessibility.id)
        mvc
            .sccRequest("/deleteAccessibility", params, user = user)
            .andExpect {
                status { isBadRequest() }
            }
    }
}
