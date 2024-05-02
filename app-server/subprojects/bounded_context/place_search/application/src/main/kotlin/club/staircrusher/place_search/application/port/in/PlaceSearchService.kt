package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.accessibility.domain.model.AccessibilityScore
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.place.application.port.`in`.BuildingService
import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.LocationUtils

@Component
class PlaceSearchService(
    private val placeService: PlaceService,
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
        siGunGuId: String?,
        eupMyeonDongId: String?,
        sort: String?,
        limit: Int?,
    ): List<SearchPlacesResult> {
        val places = placeService.findAllByKeyword(
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
                placeService.findAllByKeyword(
                    keyword = searchText,
                    option = MapsService.SearchByKeywordOption(),
                )
            } else {
                it
            }
        }.let {
            if (limit != null) it.take(limit)
            else it
        }
        return places.toSearchPlacesResult(currentLocation)
    }

    suspend fun listPlacesInBuilding(buildingId: String): List<SearchPlacesResult> {
        val buildingAddress = buildingService.getById(buildingId)?.address?.toString()
            ?: throw IllegalArgumentException("Building of id $buildingId does not exist.")
        val placesBySearch = placeService.findAllByKeyword(buildingAddress, MapsService.SearchByKeywordOption())
        val placesInPersistence = placeService.findByBuildingId(buildingId)
        return (placesBySearch + placesInPersistence)
            .removeDuplicates()
            .toSearchPlacesResult(currentLocation = null)
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

    private fun List<Place>.removeDuplicates(): List<Place> {
        return associateBy { it.id }.values.toList()
    }
}
