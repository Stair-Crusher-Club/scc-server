package club.staircrusher.place.application.port.`in`.place

import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.place.domain.model.place.Building
import club.staircrusher.place.domain.model.place.Place
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
class PlaceCrawler(
    private val placeApplicationService: PlaceApplicationService,
) {
    /**
     * 지정된 지역으로 그냥 검색하면 너무 많은 장소가 누락되는 것으로 확인되었다.
     * 장소 누락을 최대한 방지하기 위해, 다음과 같은 로직을 태운다.
     * 1. 지정된 지역을 여러 청크로 나눠서 카테고리 검색 -> 해당 지역 내의 장소 목록을 1차 획득
     * 2. 1에서 획득한 장소들의 건물 주소로 키워드 검색 -> 장소 목록을 2차 획득
     * 3. 1과 2에서 획득한 장소들을 merge & remove duplicate
     */
    suspend fun crawlPlacesInCircle(centerLocation: Location, radiusMeters: Int): List<Place> {
        val radius = Length.ofMeters(radiusMeters)
        val placesByCategorySearch = crawlPlacesInCircleByCategorySearch(centerLocation, radius)

        val buildingsInRegion = placesByCategorySearch.map { it.building }.removeDuplicates { it.id }
        val placesByBuildingAddressSearch = crawlPlacesByBuildingAddressKeywordSearch(buildingsInRegion)
            .filter { LocationUtils.calculateDistance(it.location, centerLocation) <= radius }

        return (placesByCategorySearch + placesByBuildingAddressSearch)
            .removeDuplicates { it.id }
            .filterClosed()
    }

    suspend fun crawlPlacesInPolygon(points: List<Location>): List<Place> {
        val placesByCategorySearch = crawlPlacesInPolygonByCategorySearch(points)

        val buildingsInRegion = placesByCategorySearch.map { it.building }.removeDuplicates { it.id }
        val placesByBuildingAddressSearch = crawlPlacesByBuildingAddressKeywordSearch(buildingsInRegion)
            .filter { LocationUtils.isInPolygon(points, it.location) }

        return (placesByCategorySearch + placesByBuildingAddressSearch)
            .removeDuplicates { it.id }
            .filterClosed()
    }

    suspend fun crossValidatePlaces(places: List<Place>): List<Boolean> = coroutineScope {
        places
            .map { async { placeApplicationService.crossValidate(it, MapsService.SearchByKeywordOption()) } }
            .awaitAll()
    }

    private suspend fun crawlPlacesInCircleByCategorySearch(centerLocation: Location, radius: Length): List<Place> {
        val leftBottomLocation = centerLocation.minusLng(radius).minusLat(radius)
        val rightTopLocation = centerLocation.plusLng(radius).plusLat(radius)
        return crawlPlacesInRectangleByCategorySearch(leftBottomLocation, rightTopLocation)
            .filter { LocationUtils.calculateDistance(it.location, centerLocation) <= radius }
    }

    private suspend fun crawlPlacesInPolygonByCategorySearch(points: List<Location>): List<Place> {
        val leftBottomLocation = Location(
            lng = points.minOf { it.lng },
            lat = points.minOf { it.lat },
        )
        val rightTopLocation = Location(
            lng = points.maxOf { it.lng },
            lat = points.maxOf { it.lat },
        )
        return crawlPlacesInRectangleByCategorySearch(leftBottomLocation, rightTopLocation)
            .filter { LocationUtils.isInPolygon(points, it.location) }
    }

    private suspend fun crawlPlacesInRectangleByCategorySearch(leftBottomLocation: Location, rightTopLocation: Location): List<Place> {
        val chunkCount = determineChunkCount(leftBottomLocation, rightTopLocation)
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
                coroutineScope {
                    crawlingTargetPlaceCategories
                        .map {
                            async {
                                placeApplicationService.findAllByCategory(
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
                }
            }
            .removeDuplicates { it.id }
    }

    private suspend fun crawlPlacesByBuildingAddressKeywordSearch(buildings: List<Building>): List<Place> {
        return coroutineScope {
            buildings
                .map { building ->
                    async {
                        placeApplicationService.findAllByKeyword(
                            keyword = building.address.toString(),
                            option = MapsService.SearchByKeywordOption(),
                        )
                    }
                }
                .let { awaitAll(*it.toTypedArray()) }
                .flatten()
        }
            .filter { it.category in crawlingTargetPlaceCategories }
    }

    @Suppress("MagicNumber") private val chunkTargetLength = Length.ofMeters(150)
    private fun determineChunkCount(leftBottomLocation: Location, rightTopLocation: Location): Int {
        val diagonalLength = LocationUtils.calculateDistance(leftBottomLocation, rightTopLocation)
        val estimatedSideLength = diagonalLength.meter / 1.414
        return ceil(estimatedSideLength / chunkTargetLength.meter).toInt()
    }

    private fun getAvgLocation(l1: Location, l2: Location, l2LngRatio: Double, l2LatRatio: Double): Location {
        return Location(
            lng = l1.lng * (1 - l2LngRatio) + l2.lng * l2LngRatio,
            lat = l1.lat * (1 - l2LatRatio) + l2.lat * l2LatRatio,
        )
    }

    private fun List<Place>.filterClosed(): List<Place> {
        val closedPlaceIds = placeApplicationService.findAllByIds(this.map { it.id })
            .filter { it.isClosed }
            .map { it.id }
        return this.filter { it.id !in closedPlaceIds }
    }

    private fun <T, ID> List<T>.removeDuplicates(idGetter: (T) -> ID): List<T> {
        return associateBy { idGetter(it) }.values.toList()
    }

    companion object {
        val crawlingTargetPlaceCategories = listOf(
            PlaceCategory.RESTAURANT,
            PlaceCategory.CAFE,
            PlaceCategory.MARKET,
            PlaceCategory.HOSPITAL,
            PlaceCategory.PHARMACY,
            PlaceCategory.CONVENIENCE_STORE
        )
    }
}
