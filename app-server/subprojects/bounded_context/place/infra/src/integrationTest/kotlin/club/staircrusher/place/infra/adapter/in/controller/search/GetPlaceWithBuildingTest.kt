package club.staircrusher.place.infra.adapter.`in`.controller.search

import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.place.infra.adapter.`in`.controller.search.base.PlaceSearchITBase
import club.staircrusher.stdlib.testing.SccRandom
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetPlaceWithBuildingTest : PlaceSearchITBase() {
    @Test
    fun 정상적인동작() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val (building, place) = transactionManager.doInTransaction {
            val building = testDataGenerator.createBuilding()
            testDataGenerator.registerBuildingAccessibilityIfNotExists(building)
            val place = testDataGenerator.createPlace(placeName = SccRandom.string(32), building = building)
            testDataGenerator.registerPlaceAccessibility(place)
            Pair(building, place)
        }

        val params = GetAccessibilityPostRequest(
            placeId = place.id
        )
        mvc
            .sccRequest("/getPlaceWithBuilding", params, userAccount = user)
            .apply {
                val result = getResult(object : TypeReference<PlaceListItem>() {})
                assertEquals(place.id, result.place.id)
                assertEquals(building.id, result.building.id)
            }
    }
}
