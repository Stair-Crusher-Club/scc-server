package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ListPlaceCategoriesPost200Response
import club.staircrusher.api.spec.dto.PlaceCategory
import club.staircrusher.place_search.infra.adapter.`in`.controller.base.PlaceSearchITBase
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ListPlaceCategoriesTest : PlaceSearchITBase() {
    @Test
    fun testListConqueredPlaces() {
        mvc
            .sccRequest("/listPlaceCategories", requestBody = null)
            .apply {
                val result = getResult(object : TypeReference<ListPlaceCategoriesPost200Response>() {})
                Assertions.assertEquals(PlaceCategory.values().size, result.items?.size)
            }
    }
}
