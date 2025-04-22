package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.AccessibilityInfoDto
import club.staircrusher.api.spec.dto.DeleteAccessibilityPostRequest
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.domain_event.PlaceAccessibilityDeletedEvent
import club.staircrusher.domain_event.PlaceAccessibilityCommentDeletedEvent
import club.staircrusher.domain_event.BuildingAccessibilityDeletedEvent
import club.staircrusher.domain_event.BuildingAccessibilityCommentDeletedEvent
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import java.time.Duration

class DeleteAccessibilityTest : AccessibilityITBase() {

    @Autowired
    lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository

    @MockBean
    lateinit var domainEventPublisher: DomainEventPublisher

    @Test
    fun `전반적인 테스트`() {
        // given: 한 건물에 두 개의 장소 정보를 등록한다.
        val (user, place1, placeAccessibility1, buildingAccessibility) = registerAccessibility()
        val building = place1.building
        val (_, place2, placeAccessibility2) = registerAccessibility(overridingUser = user, overridingBuilding = building)

        // when: 첫 번째 장소 정보를 삭제한다.
        val deleteAccessibilityParams1 = DeleteAccessibilityPostRequest(placeAccessibilityId = placeAccessibility1.id)
        mvc
            .sccRequest("/deleteAccessibility", deleteAccessibilityParams1, userAccount = user)
            .apply {
                transactionManager.doInTransaction {
                    assertNull(placeAccessibilityRepository.findByIdOrNull(placeAccessibility1.id))
                    assertNotNull(buildingAccessibilityRepository.findByIdOrNull(buildingAccessibility.id))
                }
            }

        runBlocking {
            verify(domainEventPublisher, times(1)).publishEvent(any<PlaceAccessibilityDeletedEvent>())
            verify(domainEventPublisher, times(1)).publishEvent(any<PlaceAccessibilityCommentDeletedEvent>())
            verify(domainEventPublisher, times(0)).publishEvent(any<BuildingAccessibilityDeletedEvent>())
            verify(domainEventPublisher, times(0)).publishEvent(any<BuildingAccessibilityCommentDeletedEvent>())
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
        reset(domainEventPublisher) // 메소드 호출 횟수를 리셋해준다.
        val deleteAccessibilityParams2 = DeleteAccessibilityPostRequest(placeAccessibilityId = placeAccessibility2.id)
        mvc
            .sccRequest("/deleteAccessibility", deleteAccessibilityParams2, userAccount = user)
            .apply {
                transactionManager.doInTransaction {
                    assertNull(placeAccessibilityRepository.findByIdOrNull(placeAccessibility2.id))
                    // 건물의 마지막 장소 정보가 삭제되면 건물 정보도 삭제된다.
                    assertNull(buildingAccessibilityRepository.findByIdOrNull(buildingAccessibility.id))
                }
            }

        runBlocking {
            verify(domainEventPublisher, times(1)).publishEvent(any<PlaceAccessibilityDeletedEvent>())
            verify(domainEventPublisher, times(1)).publishEvent(any<PlaceAccessibilityCommentDeletedEvent>())
            // 건물 정보는 이중으로 등록되지 않으므로 1개만 삭제되고, 건물 코멘트는 이중 등록이 되므로 2개가 삭제된다.
            verify(domainEventPublisher, times(1)).publishEvent(any<BuildingAccessibilityDeletedEvent>())
            verify(domainEventPublisher, times(2)).publishEvent(any<BuildingAccessibilityCommentDeletedEvent>())
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
        val (otherUser, _) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val params = DeleteAccessibilityPostRequest(placeAccessibilityId = placeAccessibility.id)
        mvc
            .sccRequest("/deleteAccessibility", params, userAccount = otherUser)
            .andExpect {
                status { isBadRequest() }
            }

        mvc
            .sccRequest("/deleteAccessibility", params)
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
