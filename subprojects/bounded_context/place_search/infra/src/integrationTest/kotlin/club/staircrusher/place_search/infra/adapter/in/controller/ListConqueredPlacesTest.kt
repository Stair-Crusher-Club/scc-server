package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.place_search.infra.adapter.`in`.controller.base.PlaceSearchITBase
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class ListConqueredPlacesTest : PlaceSearchITBase() {
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
                val result = getResult(object : TypeReference<List<PlaceListItem>>() {})
                assertEquals(registeredCount, result.size)
                result.forEach { item ->
                    val place = places.find { it.id == item.place.id }!!
                    assertEquals(place.building.id, item.building.id)
                    assertTrue(item.hasPlaceAccessibility)
                    assertTrue(item.hasBuildingAccessibility)
                    assertNull(item.distanceMeters)
                }
            }
    }
}
