package club.staircrusher.place.application.port.`in`.search

import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityApplicationService
import club.staircrusher.place.application.port.`in`.place.BuildingService
import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.place.application.result.SearchPlacesResult
import club.staircrusher.place.domain.model.accessibility.AccessibilityScore
import club.staircrusher.place.domain.model.place.Place
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
        userId: String? = null,
    ): List<SearchPlacesResult> {
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

        val clusteredPlaces = filterPlacesByBoundingBox(places)
        val placeIdToIsFavoriteMap = userId?.let { uid -> placeApplicationService.isFavoritePlaces(clusteredPlaces.map { it.id }, uid) } ?: emptyMap()

        return clusteredPlaces.toSearchPlacesResult(currentLocation = currentLocation, placeIdToIsFavoriteMap = placeIdToIsFavoriteMap)
            .filterWith(maxAccessibilityScore, hasSlope, isAccessibilityRegistered, limit)
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
                    isAccessibilityRegistrable = accessibilityApplicationService.isAccessibilityRegistrable(p.building),
                    isFavoritePlace = placeIdToIsFavoriteMap[p.id] ?: false
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
            val scoreChecked = maxAccessibilityScore?.let { (result.accessibilityScore ?: Double.MAX_VALUE) <= it } ?: true
            val slopeChecked = hasSlope?.let { result.placeAccessibility?.hasSlope == it } ?: true
            val accessibilityRegisteredChecked = isAccessibilityRegistered?.let { (result.placeAccessibility !== null) == it } ?: true
            scoreChecked && slopeChecked && accessibilityRegisteredChecked
        }.let {
            if (limit != null) it.take(limit) else it
        }
    }

    private fun List<Place>.removeDuplicates(): List<Place> {
        return associateBy { it.id }.values.toList()
    }

    private fun filterPlacesByBoundingBox(places: List<Place>): List<Place> {
        if (places.isEmpty()) return places

        val boundingBox = getBoundingBox(places)
        if (boundingBox.area <= BOUNDING_BOX_AREA_THRESHOLD) {
            return places
        }

        val mutablePlaces = places.toMutableList()
        var area = boundingBox.area
        while (area > BOUNDING_BOX_AREA_THRESHOLD && mutablePlaces.size > 1) {
            val farthestPlace = mutablePlaces.maxByOrNull { place ->
                val distance = LocationUtils.calculateDistance(boundingBox.center, place.location)
                distance.meter
            } ?: break
            mutablePlaces.remove(farthestPlace)
            val newBoundingBox = getBoundingBox(mutablePlaces)
            area = newBoundingBox.area
        }

        return mutablePlaces.toList()
    }

    private fun getBoundingBox(places: List<Place>): BoundingBox {
        val minLat = places.minOf { it.location.lat }
        val maxLat = places.maxOf { it.location.lat }
        val minLon = places.minOf { it.location.lng }
        val maxLon = places.maxOf { it.location.lng }
        return BoundingBox(minLat, maxLat, minLon, maxLon)
    }

    private data class BoundingBox(
        val minLat: Double,
        val maxLat: Double,
        val minLng: Double,
        val maxLng: Double
    ) {
        val center: Location
            get() = Location((minLng + maxLng) / 2, (minLat + maxLat) / 2)

        val area: Double
            get() {
                val height = LocationUtils.calculateDistance(Location(minLng, minLat), Location(minLng, maxLat))
                val width = LocationUtils.calculateDistance(Location(minLng, minLat), Location(maxLng, minLat))
                return height.meter * width.meter
            }
    }

    companion object {
        private const val PLACE_SEARCH_MAX_RADIUS = 20000
        // Threshold for ~2 km^2 area
        private const val BOUNDING_BOX_AREA_THRESHOLD = 2_000_000
    }
}
