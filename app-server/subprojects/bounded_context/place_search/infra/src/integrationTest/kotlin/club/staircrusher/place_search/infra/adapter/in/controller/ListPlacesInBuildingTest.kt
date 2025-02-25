package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ListPlacesInBuildingPostRequest
import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.place_search.infra.adapter.`in`.controller.base.PlaceSearchITBase
import club.staircrusher.stdlib.testing.SccRandom
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ListPlacesInBuildingTest : PlaceSearchITBase() {
    @Test
    fun testListPlacesInBuilding() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val placesCount = 10
        val accessibilityRegisteredPlaceIds = mutableSetOf<String>()
        val (building, places) = transactionManager.doInTransaction {
            val building = testDataGenerator.createBuilding()
            testDataGenerator.registerBuildingAccessibilityIfNotExists(building)
            val places = (1..placesCount).map {
                val place = testDataGenerator.createPlace(placeName = SccRandom.string(32), building = building)
                if (it % 3 == 0) {
                    testDataGenerator.registerPlaceAccessibility(place)
                    accessibilityRegisteredPlaceIds.add(place.id)
                }
                place
            }
            Pair(building, places)
        }

        val params = ListPlacesInBuildingPostRequest(
            buildingId = building.id
        )
        mvc
            .sccRequest("/listPlacesInBuilding", params, userAccount = user)
            .apply {
                val result = getResult(object : TypeReference<List<PlaceListItem>>() {})
                assertEquals(placesCount, result.size)
                result.forEach { item ->
                    val place = places.find { it.id == item.place.id }!!
                    assertEquals(building.id, item.building.id)
                    assertEquals(place.id in accessibilityRegisteredPlaceIds, item.hasPlaceAccessibility)
                    assertTrue(item.hasBuildingAccessibility)
                    assertNull(item.distanceMeters)
                }
            }

        // 로그인되어 있지 않아도 잘 동작한다.
        mvc
            .sccRequest("/listPlacesInBuilding", params)
            .andExpect {
                status {
                    isOk()
                }
            }
    }
}
