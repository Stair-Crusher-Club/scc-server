package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.accessibility.domain.model.AccessibilityScore
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.place.application.port.`in`.BuildingService
import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.LocationUtils

@Component
class PlaceSearchService(
    private val placeApplicationService: PlaceApplicationService,
    private val buildingService: BuildingService,
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    data class SearchPlacesResult(
        val place: Place,
        val buildingAccessibility: BuildingAccessibility?,
        val placeAccessibility: PlaceAccessibility?,
        val distance: Length? = null,
        val accessibilityScore: Double? = null,
        val isAccessibilityRegistrable: Boolean,
    )

    @Suppress("UnusedPrivateMember", "MagicNumber")
    suspend fun searchPlaces(
        searchText: String,
        currentLocation: Location?,
        distanceMetersLimit: Length,
        sort: String?,
        maxAccessibilityScore: Double?,
        hasSlope: Boolean?,
        isAccessibilityRegistered: Boolean?,
        limit: Int?,
    ): List<SearchPlacesResult> {
        val places = placeApplicationService.findAllByKeyword(
            searchText,
            option = MapsService.SearchByKeywordOption(
                region = currentLocation?.let {
                    MapsService.SearchByKeywordOption.CircleRegion(
                        centerLocation = it,
                        radiusMeters = distanceMetersLimit.meter.toInt(),
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
        return places.toSearchPlacesResult(currentLocation)
            .filterWith(maxAccessibilityScore, hasSlope, isAccessibilityRegistered, limit)
    }

    suspend fun listPlacesInBuilding(buildingId: String): List<SearchPlacesResult> {
        val buildingAddress = buildingService.getById(buildingId)?.address?.toString()
            ?: throw IllegalArgumentException("Building of id $buildingId does not exist.")
        val placesBySearch = placeApplicationService.findAllByKeyword(buildingAddress, MapsService.SearchByKeywordOption())
        val placesInPersistence = placeApplicationService.findByBuildingId(buildingId)
        return (placesBySearch + placesInPersistence)
            .removeDuplicates()
            .toSearchPlacesResult(currentLocation = null)
    }

    suspend fun getPlace(placeId: String): SearchPlacesResult {
        val place = placeApplicationService.findPlace(placeId) ?: throw IllegalArgumentException("Place with id $placeId does not exist.")
        return listOf(place)
            .toSearchPlacesResult(currentLocation = null)
            .first()
    }

    private fun List<Place>.toSearchPlacesResult(currentLocation: Location?): List<SearchPlacesResult> {
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
                    accessibilityScore = pa?.let { AccessibilityScore.get(pa, ba) },
                    isAccessibilityRegistrable = accessibilityApplicationService.isAccessibilityRegistrable(p.building),
                )
            }
    }

    private fun List<SearchPlacesResult>.filterWith(
        maxAccessibilityScore: Double?,
        hasSlope: Boolean?,
        isAccessibilityRegistered: Boolean?,
        limit: Int?,
    ): List<SearchPlacesResult> {
        return this.filter { result ->
            (maxAccessibilityScore == null || result.accessibilityScore ?: Double.MAX_VALUE <= maxAccessibilityScore) &&
                (hasSlope == null || result.placeAccessibility?.hasSlope == hasSlope) &&
                (isAccessibilityRegistered == null || (result.placeAccessibility !== null) == isAccessibilityRegistered)
        }.let {
            if (limit != null) it.take(limit) else it
        }
    }

    private fun List<Place>.removeDuplicates(): List<Place> {
        return associateBy { it.id }.values.toList()
    }
}
