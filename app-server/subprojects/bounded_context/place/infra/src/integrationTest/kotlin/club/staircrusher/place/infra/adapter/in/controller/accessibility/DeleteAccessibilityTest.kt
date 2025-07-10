package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.AccessibilityInfoDto
import club.staircrusher.api.spec.dto.DeleteAccessibilityPostRequest
import club.staircrusher.api.spec.dto.DeleteBuildingAccessibilityPostRequest
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.domain_event.BuildingAccessibilityCommentDeletedEvent
import club.staircrusher.domain_event.BuildingAccessibilityDeletedEvent
import club.staircrusher.domain_event.PlaceAccessibilityCommentDeletedEvent
import club.staircrusher.domain_event.PlaceAccessibilityDeletedEvent
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull

class DeleteAccessibilityTest : AccessibilityITBase() {

    @Autowired
    lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository

    @MockBean
    lateinit var domainEventPublisher: DomainEventPublisher

    @Test
    fun `장소 정보를 삭제하는 경우`() {
        val (user, place, placeAccessibility, buildingAccessibility) = registerAccessibility()

        val deletePlaceAccessibilityParams = DeleteAccessibilityPostRequest(placeAccessibilityId = placeAccessibility.id)
        mvc
            .sccRequest("/deletePlaceAccessibility", deletePlaceAccessibilityParams, userAccount = user)
            .apply {
                transactionManager.doInTransaction {
                    assertNull(placeAccessibilityRepository.findByIdOrNull(placeAccessibility.id))
                    assertNotNull(buildingAccessibilityRepository.findByIdOrNull(buildingAccessibility.id))
                }
            }

        runBlocking {
            verify(domainEventPublisher, times(1)).publishEvent(any<PlaceAccessibilityDeletedEvent>())
            verify(domainEventPublisher, times(1)).publishEvent(any<PlaceAccessibilityCommentDeletedEvent>())
            verify(domainEventPublisher, times(0)).publishEvent(any<BuildingAccessibilityDeletedEvent>())
            verify(domainEventPublisher, times(0)).publishEvent(any<BuildingAccessibilityCommentDeletedEvent>())
        }
        val getAccessibilityParams = GetAccessibilityPostRequest(placeId = place.id)
        mvc
            .sccAnonymousRequest("/getAccessibility", getAccessibilityParams)
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertNull(result.placeAccessibility)
                assertTrue(result.placeAccessibilityComments.isEmpty())
                assertNotNull(result.buildingAccessibility)
                assertTrue(result.buildingAccessibilityComments.isNotEmpty())
            }
    }

    @Test
    fun `건물 정보를 삭제하는 경우`() {
        val (user, place, placeAccessibility, buildingAccessibility) = registerAccessibility()

        val deleteBuildingAccessibilityParams = DeleteBuildingAccessibilityPostRequest(buildingAccessibilityId = buildingAccessibility.id)
        mvc
            .sccRequest("/deleteBuildingAccessibility", deleteBuildingAccessibilityParams, userAccount = user)
            .apply {
                transactionManager.doInTransaction {
                    assertNotNull(placeAccessibilityRepository.findByIdOrNull(placeAccessibility.id))
                    assertNull(buildingAccessibilityRepository.findByIdOrNull(buildingAccessibility.id))
                }
            }

        runBlocking {
            verify(domainEventPublisher, times(0)).publishEvent(any<PlaceAccessibilityDeletedEvent>())
            verify(domainEventPublisher, times(0)).publishEvent(any<PlaceAccessibilityCommentDeletedEvent>())
            verify(domainEventPublisher, times(1)).publishEvent(any<BuildingAccessibilityDeletedEvent>())
            verify(domainEventPublisher, times(1)).publishEvent(any<BuildingAccessibilityCommentDeletedEvent>())
        }
        val getAccessibilityParams = GetAccessibilityPostRequest(placeId = place.id)
        mvc
            .sccAnonymousRequest("/getAccessibility", getAccessibilityParams)
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertNotNull(result.placeAccessibility)
                assertTrue(result.placeAccessibilityComments.isNotEmpty())
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
            .sccRequest("/deletePlaceAccessibility", params, userAccount = otherUser)
            .andExpect {
                status { isBadRequest() }
            }

        mvc
            .sccRequest("/deletePlaceAccessibility", params)
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
