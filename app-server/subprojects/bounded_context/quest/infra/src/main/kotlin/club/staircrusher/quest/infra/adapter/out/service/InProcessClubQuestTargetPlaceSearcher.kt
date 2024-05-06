package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.LocationUtils
import club.staircrusher.stdlib.place.PlaceCategory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.ceil

@Component
class InProcessClubQuestTargetPlaceSearcher(
    private val placeService: PlaceService,
) : ClubQuestTargetPlacesSearcher {
    private val targetPlaceCategories = listOf(
        PlaceCategory.RESTAURANT,
        PlaceCategory.CAFE,
        PlaceCategory.MARKET,
        PlaceCategory.HOSPITAL,
        PlaceCategory.PHARMACY,
        PlaceCategory.CONVENIENCE_STORE
    )

    /**
     * 지정된 지역으로 그냥 검색하면 너무 많은 장소가 누락되는 것으로 확인되었다.
     * 장소 누락을 최대한 방지하기 위해, 다음과 같은 로직을 태운다.
     * 1. 지정된 지역을 여러 청크로 나눠서 카테고리 검색 -> 해당 지역 내의 장소 목록을 1차 획득
     * 2. 1에서 획득한 장소들의 건물 주소로 키워드 검색 -> 장소 목록을 2차 획득
     * 3. 1과 2에서 획득한 장소들을 merge & remove duplicate
     */
    override suspend fun searchPlaces(centerLocation: Location, radiusMeters: Int): List<Place> {
        val radius = Length.ofMeters(radiusMeters)
        val placesByCategorySearch = searchPlacesInRegionByCategorySearch(centerLocation, radius)
        val buildingsInRegion = placesByCategorySearch.map { it.building }.removeDuplicates { it.id }
        val placesByBuildingAddressSearch = searchPlacesByBuildingAddressKeywordSearch(buildingsInRegion, centerLocation, radius)
        return (placesByCategorySearch + placesByBuildingAddressSearch)
            .removeDuplicates { it.id }
    }

    override suspend fun crossValidatePlaces(places: List<Place>): List<Boolean> = coroutineScope {
        places
            .map { async { placeService.crossValidate(it, MapsService.SearchByKeywordOption()) } }
            .awaitAll()
    }

    private suspend fun searchPlacesInRegionByCategorySearch(centerLocation: Location, radius: Length): List<Place> {
        val leftBottomLocation = centerLocation.minusLng(radius).minusLat(radius)
        val rightTopLocation = centerLocation.plusLng(radius).plusLat(radius)

        val chunkCount = determineChunkCount(radius)
        val chunkedRectangles = (1..chunkCount).flatMap { lngIdx ->
            (1..chunkCount).map { latIdx ->
                val chunkLeftBottomLocation = getAvgLocation(
                    leftBottomLocation,
                    rightTopLocation,
                    (lngIdx - 1).toDouble() / chunkCount,
                    (latIdx - 1).toDouble() / chunkCount,
                )
                val chunkRightTopLocation = getAvgLocation(
                    leftBottomLocation,
                    rightTopLocation,
                    lngIdx.toDouble() / chunkCount,
                    latIdx.toDouble() / chunkCount,
                )
                Pair(chunkLeftBottomLocation, chunkRightTopLocation)
            }
        }
        return chunkedRectangles
            .flatMap { (leftBottomLocation, rightTopLocation) ->
                getPlacesInRectangleByCategorySearch(leftBottomLocation, rightTopLocation)
            }
            .removeDuplicates { it.id }
            .filter { LocationUtils.calculateDistance(it.location, centerLocation) <= radius }
    }

    private suspend fun getPlacesInRectangleByCategorySearch(leftBottomLocation: Location, rightTopLocation: Location): List<Place> {
        return coroutineScope {
            targetPlaceCategories
                .map {
                    async {
                        placeService.findAllByCategory(
                            category = it,
                            option = MapsService.SearchByCategoryOption(
                                region = MapsService.SearchByCategoryOption.RectangleRegion(
                                    leftBottomLocation = leftBottomLocation,
                                    rightTopLocation = rightTopLocation,
                                ),
                            )
                        )
                    }
                }
                .let { awaitAll(*it.toTypedArray()) }
                .flatten()
                .removeDuplicates { it.id }
        }
    }

    private suspend fun searchPlacesByBuildingAddressKeywordSearch(buildings: List<Building>, centerLocation: Location, radius: Length): List<Place> {
        return coroutineScope {
            buildings
                .map { building ->
                    async {
                        placeService.findAllByKeyword(
                            keyword = building.address.toString(),
                            option = MapsService.SearchByKeywordOption(),
                        )
                    }
                }
                .let { awaitAll(*it.toTypedArray()) }
                .flatten()
        }
            .filter { it.category in targetPlaceCategories }
            .filter { LocationUtils.calculateDistance(it.location, centerLocation) <= radius }
    }

    @Suppress("MagicNumber") private val chunkTargetLength = Length.ofMeters(150)
    private fun determineChunkCount(radius: Length): Int {
        return ceil(radius.meter * 2 / chunkTargetLength.meter).toInt()
    }

    private fun getAvgLocation(l1: Location, l2: Location, l2LngRatio: Double, l2LatRatio: Double): Location {
        return Location(
            lng = l1.lng * (1 - l2LngRatio) + l2.lng * l2LngRatio,
            lat = l1.lat * (1 - l2LatRatio) + l2.lat * l2LatRatio,
        )
    }

    private fun <T, ID> List<T>.removeDuplicates(idGetter: (T) -> ID): List<T> {
        return associateBy { idGetter(it) }.values.toList()
    }
}
