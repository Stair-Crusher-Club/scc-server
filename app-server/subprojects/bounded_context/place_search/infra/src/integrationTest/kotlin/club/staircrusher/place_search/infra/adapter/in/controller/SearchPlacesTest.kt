package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.SearchPlacesPost200Response
import club.staircrusher.api.spec.dto.SearchPlacesPostRequest
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place_search.infra.adapter.`in`.controller.base.PlaceSearchITBase
import club.staircrusher.stdlib.place.PlaceCategory
import club.staircrusher.stdlib.testing.SccRandom
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean

class SearchPlacesTest : PlaceSearchITBase() {
    @MockBean
    private lateinit var mapsService: MapsService

    @Test
    fun testSearchPlaces() = runBlocking {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = SccRandom.string(32))
        }
        val searchText = place.name.substring(2, 5)
        val radiusMeters = 500

        Mockito.`when`(
            mapsService.findAllByKeyword(
                searchText,
                MapsService.SearchByKeywordOption(
                    MapsService.SearchByKeywordOption.CircleRegion(
                        centerLocation = place.location,
                        radiusMeters = radiusMeters,
                    ),
                ),
            )
        ).thenReturn(listOf(place))

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
                assertTrue(result.items!![0].isAccessibilityRegistrable)
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

    @Test
    fun `서울, 성남외의 지역은 검색되지만 접근성 정보는 등록이 불가능하다`() = runBlocking {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(
                placeName = SccRandom.string(32),
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
        val searchText = place.name.substring(2, 5)
        val radiusMeters = 500

        Mockito.`when`(
            mapsService.findAllByKeyword(
                searchText,
                MapsService.SearchByKeywordOption(
                    MapsService.SearchByKeywordOption.CircleRegion(
                        centerLocation = place.location,
                        radiusMeters = radiusMeters,
                    ),
                ),
            )
        ).thenReturn(listOf(place))

        val params = SearchPlacesPostRequest(
            searchText = searchText,
            distanceMetersLimit = radiusMeters,
            currentLocation = place.location.toDTO(),
        )
        mvc.sccRequest("/searchPlaces", params, user = user)
            .apply {
                val result = getResult(SearchPlacesPost200Response::class)
                assertEquals(1, result.items!!.size)

                val searchResult = result.items!!.first()
                assertEquals(place.id, searchResult.place.id)
                assertNotNull(searchResult.distanceMeters)
                assertFalse(searchResult.isAccessibilityRegistrable)
            }

        Unit
    }


    @Test
    fun `키워드 검색 요청 카테고리만 내려준다`() = runBlocking {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val places = transactionManager.doInTransaction {
            PlaceCategory.values().map { category ->
                testDataGenerator.createBuildingAndPlace(
                    placeName = SccRandom.string(32),
                    buildingAddress = BuildingAddress(
                        siDo = "경기도",
                        siGunGu = "수원시",
                        eupMyeonDong = "영통동",
                        li = "",
                        roadName = "봉영로",
                        mainBuildingNumber = "83",
                        subBuildingNumber = "21",
                    ),
                    category = category
                )
            }
        }
        val searchText = places.first().name.substring(2, 5)
        val radiusMeters = 500
        Mockito.`when`(
            mapsService.findAllByKeyword(
                searchText,
                MapsService.SearchByKeywordOption(
                    region = MapsService.SearchByKeywordOption.CircleRegion(
                        centerLocation = places.first().location,
                        radiusMeters = radiusMeters,
                    ),
                    category = null
                ),
            )
        ).thenReturn(places)
        val selectedRandomCategory = PlaceCategory.from(
            club.staircrusher.api.spec.dto.PlaceCategory.values().random()
        )!!
        Mockito.`when`(
            mapsService.findAllByKeyword(
                searchText,
                MapsService.SearchByKeywordOption(
                    region = MapsService.SearchByKeywordOption.CircleRegion(
                        centerLocation = places.first().location,
                        radiusMeters = radiusMeters,
                    ),
                    category = selectedRandomCategory
                ),
            )
        ).thenReturn(places.filter { it.category == selectedRandomCategory })

        // 카테고리가 없는 경우 모두 내려준다.
        val params1 = SearchPlacesPostRequest(
            searchText = searchText,
            distanceMetersLimit = radiusMeters,
            currentLocation = places.first().location.toDTO(),
            category = null
        )
        mvc.sccRequest("/searchPlaces", params1, user = user)
            .apply {
                val result = getResult(SearchPlacesPost200Response::class)
                assertEquals(places.size, result.items!!.size)
            }

        // 카테고리가 있는 경우 카테고리에 해당하는 place 만 내려준다.
        val params2 = SearchPlacesPostRequest(
            searchText = searchText,
            distanceMetersLimit = radiusMeters,
            currentLocation = places.first().location.toDTO(),
            category = selectedRandomCategory.toDTO()?.type
        )
        mvc.sccRequest("/searchPlaces", params2, user = user)
            .apply {
                val result = getResult(SearchPlacesPost200Response::class)
                assertEquals(1, result.items!!.size)

                val searchResult = result.items!!.first()
                assertEquals(places.first { it.category == selectedRandomCategory }.id, searchResult.place.id)
            }
        Unit
    }
}
