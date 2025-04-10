package club.staircrusher.place.infra.adapter.`in`.controller.search

import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.api.spec.dto.SearchPlaceFilterDto
import club.staircrusher.api.spec.dto.SearchPlacesPost200Response
import club.staircrusher.api.spec.dto.SearchPlacesPostRequest
import club.staircrusher.place.application.port.out.place.persistence.PlaceRepository
import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.place.domain.model.place.BuildingAddress
import club.staircrusher.place.infra.adapter.`in`.controller.search.base.PlaceSearchITBase
import club.staircrusher.stdlib.testing.SccRandom
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean

class SearchPlacesTest : PlaceSearchITBase() {
    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @SpyBean
    private lateinit var mapsService: MapsService

    @Test
    fun testSearchPlaces() = runBlocking {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
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
        mvc.sccRequest("/searchPlaces", params, userAccount = user)
            .apply {
                val result = getResult(SearchPlacesPost200Response::class)
                assertEquals(1, result.items!!.size)
                assertEquals(place.id, result.items!![0].place.id)
                assertNotNull(result.items!![0].distanceMeters)
                assertTrue(result.items!![0].isAccessibilityRegistrable)
            }

        // 로그인되어 있지 않아도 잘 동작한다.
        mvc.sccAnonymousRequest("/searchPlaces", params)
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
            testDataGenerator.createIdentifiedUser().account
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
        mvc.sccRequest("/searchPlaces", params, userAccount = user)
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
    fun `폐업된 장소는 보여주지 않는다`() = runBlocking {
        // given
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
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
        mvc.sccRequest("/searchPlaces", params, userAccount = user)
            .apply {
                val result = getResult(SearchPlacesPost200Response::class)
                assertEquals(1, result.items!!.size)
            }

        // when - isClosed
        transactionManager.doInTransaction {
            placeRepository.findById(place.id).get()
                .apply { setIsClosed(true) }
                .apply { setIsNotAccessible(false) }
                .apply { placeRepository.save(this) }
        }

        // then - 폐업되면 검색에 걸리지 않는다.
        mvc.sccRequest("/searchPlaces", params, userAccount = user)
            .apply {
                val result = getResult(SearchPlacesPost200Response::class)
                assertEquals(0, result.items!!.size)
            }

        // when - isNotAccessible
        transactionManager.doInTransaction {
            placeRepository.findById(place.id).get()
                .apply { setIsClosed(false) }
                .apply { setIsNotAccessible(true) }
                .apply { placeRepository.save(this) }
        }

        // then - 접근 불가여도 검색에 걸린다.
        mvc.sccRequest("/searchPlaces", params, userAccount = user)
            .apply {
                val result = getResult(SearchPlacesPost200Response::class)
                assertEquals(1, result.items!!.size)
            }

        Unit
    }

    @Test
    fun `필터 인자에 따라 장소가 노출된다`() = runBlocking {
        // given
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val placePrefix = SccRandom.string(4)
        val placeWithSlope = transactionManager.doInTransaction {
            val place = testDataGenerator.createBuildingAndPlace(placeName = placePrefix + SccRandom.string(20))
            testDataGenerator.registerPlaceAccessibility(place, userAccount = user, hasSlope = true)
            place
        }
        val placeWithoutSlope = transactionManager.doInTransaction {
            val place = testDataGenerator.createBuildingAndPlace(placeName = placePrefix + SccRandom.string(20))
            testDataGenerator.registerPlaceAccessibility(place, userAccount = user, hasSlope = false)
            place
        }
        val placeNotRegistered = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = placePrefix + SccRandom.string(20))
        }
        val radiusMeters = 500

        Mockito.`when`(
            mapsService.findAllByKeyword(
                placePrefix,
                MapsService.SearchByKeywordOption(
                    MapsService.SearchByKeywordOption.CircleRegion(
                        centerLocation = placeWithSlope.location,
                        radiusMeters = radiusMeters,
                    ),
                ),
            )
        ).thenReturn(listOf(placeWithSlope, placeWithoutSlope, placeNotRegistered))

        fun requestWithFilter(filter: SearchPlaceFilterDto): List<PlaceListItem> {
            val params = SearchPlacesPostRequest(
                searchText = placePrefix,
                distanceMetersLimit = radiusMeters,
                currentLocation = placeWithSlope.location.toDTO(),
                filters = filter,
            )
            return mvc.sccRequest("/searchPlaces", params, userAccount = user)
                .getResult(SearchPlacesPost200Response::class).items!!
        }

        requestWithFilter(filter = SearchPlaceFilterDto())
            .apply {
                assertEquals(3, size)
            }
        requestWithFilter(filter = SearchPlaceFilterDto(hasSlope = true))
            .apply {
                assertEquals(1, size)
                assertTrue(any { it.place.id == placeWithSlope.id })
            }
        requestWithFilter(filter = SearchPlaceFilterDto(hasSlope = false))
            .apply {
                assertEquals(1, size)
                assertTrue(any { it.place.id == placeWithoutSlope.id })
            }
        requestWithFilter(filter = SearchPlaceFilterDto(isRegistered = true))
            .apply {
                assertEquals(2, size)
                assertTrue(any { it.place.id == placeWithSlope.id })
                assertTrue(any { it.place.id == placeWithoutSlope.id })
            }
        requestWithFilter(filter = SearchPlaceFilterDto(isRegistered = false))
            .apply {
                assertEquals(1, size)
                assertTrue(any { it.place.id == placeNotRegistered.id })
            }
        Unit
    }

    @Test
    fun `즐겨찾기 정보를 내려준다`() = runBlocking {
        val user = transactionManager.doInTransaction { testDataGenerator.createIdentifiedUser().account }
        val favoritePlace = transactionManager.doInTransaction { testDataGenerator.createBuildingAndPlace(placeName = SccRandom.string(32)) }
        transactionManager.doInTransaction { testDataGenerator.createPlaceFavorite(userId = user.id, placeId = favoritePlace.id) }
        val notFavoritePlace = transactionManager.doInTransaction { testDataGenerator.createBuildingAndPlace(placeName = SccRandom.string(32)) }
        val searchText = notFavoritePlace.name.substring(2, 5)
        val radiusMeters = 500

        Mockito.`when`(
            mapsService.findAllByKeyword(
                searchText,
                MapsService.SearchByKeywordOption(
                    MapsService.SearchByKeywordOption.CircleRegion(centerLocation = favoritePlace.location, radiusMeters = radiusMeters,),
                ),
            )
        ).thenReturn(listOf(favoritePlace, notFavoritePlace))

        val params = SearchPlacesPostRequest(searchText = searchText, distanceMetersLimit = radiusMeters, currentLocation = favoritePlace.location.toDTO(),)
        mvc.sccRequest("/searchPlaces", params, userAccount = user)
            .apply {
                val result = getResult(SearchPlacesPost200Response::class)
                assertEquals(2, result.items!!.size)
                assertEquals(favoritePlace.id, result.items!![0].place.id)
                assertNotNull(result.items!![0].distanceMeters)
                assertTrue(result.items!![0].isAccessibilityRegistrable)
                assertEquals(result.items!![0].place.isFavorite, true)
                assertEquals(result.items!![1].place.isFavorite, false)
            }
        Unit
    }
}
