package club.staircrusher.place.application.port.`in`.search

import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityApplicationService
import club.staircrusher.place.application.port.`in`.accessibility.place_review.PlaceReviewService
import club.staircrusher.place.application.port.`in`.place.BuildingService
import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.place.application.result.SearchPlacesResult
import club.staircrusher.place.domain.model.accessibility.AccessibilityScore
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.place.domain.model.search.SearchPlaceFilter
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.LocationUtils
import club.staircrusher.stdlib.place.PlaceCategory

@Component
class PlaceSearchService(
    private val placeApplicationService: PlaceApplicationService,
    private val buildingService: BuildingService,
    private val placeReviewService: PlaceReviewService,
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {

    @Suppress("UnusedPrivateMember", "MagicNumber")
    suspend fun searchPlaces(
        searchText: String,
        currentLocation: Location?,
        distanceMetersLimit: Length,
        sort: String?,
        filter: SearchPlaceFilter?,
        limit: Int?,
        userId: String? = null,
    ): List<SearchPlacesResult> {
        val placeCategory = PlaceCategory.valueOfOrNull(searchText)
        val places = if (placeCategory != null && currentLocation != null) {
            searchPlacesByCategory(placeCategory, currentLocation, distanceMetersLimit)
        } else {
            searchPlacesByKeyword(searchText, currentLocation, distanceMetersLimit, sort)
        }
        val placeIdToIsFavoriteMap = userId?.let { uid -> placeApplicationService.isFavoritePlaces(places.map { it.id }, uid) } ?: emptyMap()

        return places.toSearchPlacesResult(currentLocation = currentLocation, placeIdToIsFavoriteMap = placeIdToIsFavoriteMap)
            .filterWith(filter, limit)
            .let { if (sort == "ACCESSIBILITY_SCORE") it.sortedBy { it.accessibilityScore } else it }
    }

    suspend fun listPlacesInBuilding(buildingId: String, userId: String? = null): List<SearchPlacesResult> {
        val buildingAddress = buildingService.getById(buildingId)?.address?.toString()
            ?: throw IllegalArgumentException("Building of id $buildingId does not exist.")
        val placesBySearch = placeApplicationService.findAllByKeyword(buildingAddress, MapsService.SearchByKeywordOption())
        val placesInPersistence = placeApplicationService.findByBuildingId(buildingId)
        val places = (placesBySearch + placesInPersistence).removeDuplicates()

        val placeIdToIsFavoriteMap = userId?.let { uid -> placeApplicationService.isFavoritePlaces(places.map { it.id }, uid) } ?: emptyMap()

        return places
            .toSearchPlacesResult(currentLocation = null, placeIdToIsFavoriteMap = placeIdToIsFavoriteMap)
    }

    fun getPlace(placeId: String, userId: String? = null): SearchPlacesResult {
        val place = placeApplicationService.findPlace(placeId) ?: throw IllegalArgumentException("Place with id $placeId does not exist.")
        val isFavorite = userId?.let { uid -> placeApplicationService.isFavoritePlace(placeId, uid) } ?: false
        return listOf(place)
            .toSearchPlacesResult(currentLocation = null, mapOf(placeId to isFavorite))
            .first()
    }

    fun listPlaces(placeIds: List<String>, userId: String? = null): List<SearchPlacesResult> {
        val places = placeApplicationService.findAllByIds(placeIds)
        val placeIdToIsFavoriteMap = userId?.let { placeApplicationService.isFavoritePlaces(placeIds, it) } ?: emptyMap()
        return places.toSearchPlacesResult(currentLocation = null, placeIdToIsFavoriteMap)
    }

    private suspend fun searchPlacesByCategory(
        category: PlaceCategory,
        currentLocation: Location,
        distanceMetersLimit: Length,
    ): List<Place> {
        return placeApplicationService.findAllByCategory(
            category = category,
            option = MapsService.SearchByCategoryOption(
                region = MapsService.SearchByCategoryOption.CircleRegion(
                    centerLocation = currentLocation,
                    radiusMeters = minOf(distanceMetersLimit.meter.toInt(), PLACE_SEARCH_MAX_RADIUS),
                    sort = MapsService.SearchByCategoryOption.CircleRegion.Sort.DISTANCE,
                )
            ),
            shouldFilterClosed = true,
        )
    }

    private suspend fun searchPlacesByKeyword(
        searchText: String,
        currentLocation: Location?,
        distanceMetersLimit: Length,
        sort: String?,
    ): List<Place> {
        val places = placeApplicationService.findAllByKeyword(
            searchText,
            option = MapsService.SearchByKeywordOption(
                region = currentLocation?.let {
                    MapsService.SearchByKeywordOption.CircleRegion(
                        centerLocation = it,
                        radiusMeters = minOf(distanceMetersLimit.meter.toInt(), PLACE_SEARCH_MAX_RADIUS),
                        sort = sort
                            ?.let { MapsService.SearchByKeywordOption.CircleRegion.Sort.valueFrom(it) }
                            ?: MapsService.SearchByKeywordOption.CircleRegion.Sort.ACCURACY
                    )
                }
            ),
        ).let {
            if (it.isEmpty() && currentLocation != null) {
                // Kakao 지도 API의 경우, 최대 검색 반경이 25km밖에 되지 않는다.
                // 그래서 서울에서 제주 스타벅스를 검색하는 경우 검색 결과가 안뜨는 등의 이슈가 있다.
                // 이러한 문제를 우회하기 위해, 검색 결과가 없는 경우에는 currentLocation을 빼고 검색을 다시 시도해본다.
                placeApplicationService.findAllByKeyword(
                    keyword = searchText,
                    option = MapsService.SearchByKeywordOption(),
                )
            } else {
                it
            }
        }
        val placesInPersistence = placeApplicationService.findByNameLikeAndNotClosed(searchText, BIG_FRANCHISE_THRESHOLD)
        val combinedPlaces = (placesInPersistence + places).removeDuplicates()

        val bigFranchisePlaceCount = combinedPlaces.count { it.name.startsWith(searchText) }
        if (bigFranchisePlaceCount >= BIG_FRANCHISE_THRESHOLD && currentLocation != null) {
            // distance limit 이 20km 로 되어 있는 경우가 있어, 조금 어색한 경우가 발생하기에
            // 주변에 있는 지도에서 검색된 장소만 리턴
            return places.filter { place ->
                val distance = LocationUtils.calculateDistance(currentLocation, place.location)
                distance.meter < minOf(distanceMetersLimit.meter.toInt(), PLACE_SEARCH_MAX_RADIUS)
            }
        }

        return combinedPlaces
    }

    private fun List<Place>.toSearchPlacesResult(currentLocation: Location?, placeIdToIsFavoriteMap: Map<String, Boolean>): List<SearchPlacesResult> {
        if (this.isEmpty()) {
            return emptyList()
        }
        return accessibilityApplicationService.listPlaceAndBuildingAccessibility(this)
            .zip(this) { (pa, ba), p -> Triple(pa, ba, p) }
            .map { (pa, ba, p) ->
                SearchPlacesResult(
                    place = p,
                    buildingAccessibility = ba,
                    placeAccessibility = pa,
                    distance = currentLocation?.let { LocationUtils.calculateDistance(it, p.location) },
                    accessibilityScore = pa?.let { AccessibilityScore.get(pa, ba)?.coerceIn(0.0..5.0) },
                    isAccessibilityRegistrable = accessibilityApplicationService.isAccessibilityRegistrable(p),
                    placeReviewCount = placeReviewService.countByPlaceId(p.id),
                    isFavoritePlace = placeIdToIsFavoriteMap[p.id] ?: false
                )
            }
    }

    private fun List<SearchPlacesResult>.filterWith(
        searchPlaceFilter: SearchPlaceFilter?,
        limit: Int?,
    ): List<SearchPlacesResult> {
        return this.filter { result ->
            val scoreChecked = searchPlaceFilter?.maxAccessibilityScore?.let { (result.accessibilityScore ?: Double.MAX_VALUE) <= it } ?: true
            val slopeChecked = searchPlaceFilter?.hasSlope?.let { result.placeAccessibility?.hasSlope == it } ?: true
            val accessibilityRegisteredChecked = searchPlaceFilter?.isAccessibilityRegistered?.let { (result.placeAccessibility !== null) == it } ?: true
            scoreChecked && slopeChecked && accessibilityRegisteredChecked
        }.let {
            if (limit != null) it.take(limit) else it
        }
    }

    private fun List<Place>.removeDuplicates(): List<Place> {
        return associateBy { it.id }.values.toList()
    }

    companion object {
        private const val PLACE_SEARCH_MAX_RADIUS = 20000
        private const val BIG_FRANCHISE_THRESHOLD = 30
    }
}
