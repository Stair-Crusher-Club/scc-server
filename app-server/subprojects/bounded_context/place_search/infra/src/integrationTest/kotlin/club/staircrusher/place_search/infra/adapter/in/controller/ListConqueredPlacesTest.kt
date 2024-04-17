package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.api.spec.dto.DeleteAccessibilityPostRequest
import club.staircrusher.api.spec.dto.ListConqueredPlacesResponseDto
import club.staircrusher.place_search.infra.adapter.`in`.controller.base.PlaceSearchITBase
import club.staircrusher.testing.spring_it.mock.MockSccClock
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration


class ListConqueredPlacesTest : PlaceSearchITBase() {
    @Autowired
    lateinit var mockSccClock: MockSccClock

    @Test
    fun testListConqueredPlaces() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val registeredCount = 10
        val places = transactionManager.doInTransaction {
            (1..registeredCount).map {
                val place = testDataGenerator.createBuildingAndPlace()
                testDataGenerator.registerBuildingAndPlaceAccessibility(place, user)
                place
            }
        }

        mvc
            .sccRequest("/listConqueredPlaces", requestBody = null, user = user)
            .apply {
                val result = getResult(object : TypeReference<ListConqueredPlacesResponseDto>() {})
                assertEquals(registeredCount.toLong(), result.totalNumberOfItems)
                result.items.forEach { item ->
                    val place = places.find { it.id == item.place.id }!!
                    assertEquals(place.building.id, item.building.id)
                    assertTrue(item.hasPlaceAccessibility)
                    assertTrue(item.hasBuildingAccessibility)
                    assertNull(item.distanceMeters)
                }
            }
    }

    @Test
    fun 생성_시간_역순으로_내려준다() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val placeIdsOrderedByCreatedAtDesc = (1..10).map {
            mockSccClock.advanceTime(Duration.ofHours(1))
            transactionManager.doInTransaction {
                val place = testDataGenerator.createBuildingAndPlace()
                val (pa, _) = testDataGenerator.registerBuildingAndPlaceAccessibility(place, user)
                place to pa
            }
        }
            .sortedByDescending { it.second.createdAt }
            .map { it.first.id }

        mvc
            .sccRequest("/listConqueredPlaces", requestBody = null, user = user)
            .apply {
                val result = getResult(object : TypeReference<ListConqueredPlacesResponseDto>() {})
                assertEquals(
                    placeIdsOrderedByCreatedAtDesc,
                    result.items.map { it.place.id },
                )
            }
    }
}
