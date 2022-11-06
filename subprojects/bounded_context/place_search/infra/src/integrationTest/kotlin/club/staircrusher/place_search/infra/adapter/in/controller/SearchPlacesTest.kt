package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.SearchPlacesPost200Response
import club.staircrusher.api.spec.dto.SearchPlacesPostRequest
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place_search.infra.adapter.`in`.controller.base.PlaceSearchITBase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import kotlin.random.Random

class SearchPlacesTest : PlaceSearchITBase() {
    @MockBean
    private lateinit var mapsService: MapsService

    @Test
    fun testSearchPlaces() = runBlocking {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = Random.nextBytes(32).toString())
        }
        val searchText = place.name.substring(2, 5)
        val radiusMeters = 500

        Mockito.`when`(mapsService.findAllByKeyword(
            searchText,
            MapsService.SearchByKeywordOption(
                MapsService.SearchByKeywordOption.CircleRegion(
                    centerLocation = place.location,
                    radiusMeters = radiusMeters,
                ),
            ),
        )).thenReturn(listOf(place))

        val params = SearchPlacesPostRequest(
            searchText = searchText,
            distanceMetersLimit = radiusMeters,
            currentLocation = place.location.toDTO(),
        )
        mvc.sccRequest("/searchPlaces", params, user = user)
            .apply {
                val result = getResult(SearchPlacesPost200Response::class)
                assertEquals(1, result.items!!.size)
                assertEquals(place.id, result.items!![0].place.id)
                assertNotNull(result.items!![0].distanceMeters)
            }

        // 로그인되어 있지 않아도 잘 동작한다.
        mvc.sccRequest("/searchPlaces", params)
            .andExpect {
                status {
                    isOk()
                }
            }

        Unit
    }
}
