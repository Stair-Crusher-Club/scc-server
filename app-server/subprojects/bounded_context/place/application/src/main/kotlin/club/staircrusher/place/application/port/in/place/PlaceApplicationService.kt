package club.staircrusher.place.application.port.`in`.place

import club.staircrusher.domain_event.PlaceSearchEvent
import club.staircrusher.place.application.port.out.place.persistence.PlaceFavoriteRepository
import club.staircrusher.place.application.port.out.place.persistence.PlaceRepository
import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.WellKnownTextUtils
import club.staircrusher.stdlib.place.PlaceCategory
import club.staircrusher.stdlib.util.string.getSimilarityWith
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

@Component
class PlaceApplicationService(
    private val placeRepository: PlaceRepository,
    private val placeFavoriteRepository: PlaceFavoriteRepository,
    private val eventPublisher: DomainEventPublisher,
    private val mapsServices: List<MapsService>,
) {
    private val logger = KotlinLogging.logger {}
    private val mapsService = mapsServices.first()
    private val secondaryMapsService = mapsServices.getOrNull(1)

    fun findPlace(placeId: String): Place? {
        return placeRepository.findByIdOrNull(placeId)
    }

    suspend fun findAllByKeyword(
        keyword: String,
        option: MapsService.SearchByKeywordOption,
    ): List<Place> = coroutineScope {
        if (keyword.isBlank()) {
            return@coroutineScope emptyList()
        }

        val places = mapsService.findAllByKeyword(keyword, option)
            .mergeLocalDatabases()
            .filterClosed()
            .let {
                if (option.runCrossValidation) {
                    val existingPlaces = it.map { async { crossValidate(it, option) } }.awaitAll()
                    it.filterIndexed { index, _ -> existingPlaces[index] }
                } else {
                    it
                }
            }

        eventPublisher.publishEvent(PlaceSearchEvent(places.map(Place::toPlaceDTO)))
        places
    }

    suspend fun findAllByCategory(
        category: PlaceCategory,
        option: MapsService.SearchByCategoryOption,
    ): List<Place> {
        val places = mapsService.findAllByCategory(category, option)
            .mergeLocalDatabases()
        eventPublisher.publishEvent(PlaceSearchEvent(places.map(Place::toPlaceDTO)))
        return places
    }

    private fun List<Place>.filterClosed(): List<Place> {
        val closedPlaceIds = placeRepository.findAllById(this.map { it.id })
            .filter { it.isClosed }
            .map { it.id }
        return this.filter { it.id !in closedPlaceIds }
    }

    fun findAllByIds(placeIds: Collection<String>): List<Place> {
        return placeRepository.findAllById(placeIds).toList()
    }

    fun findByBuildingId(buildingId: String): List<Place> {
        return placeRepository.findByBuildingId(buildingId)
    }

    fun findByNameLike(keyword: String): List<Place> {
        if (keyword.isBlank() || keyword.length < MIN_KEYWORD_LENGTH) {
            return emptyList()
        }
        // DB 에서 장소를 검색하는 것은 키워드와 일치하는데 지도 API 의 결과에 나오지 않는 문제를 해결하기 위한 것이다
        // 따라서 10 개만 검색하더라도 충분하다
        val pageRequest = PageRequest.of(0, 10)
        return placeRepository.findAllByNameStartsWith(keyword, pageRequest)
            .sortedBy { it.name.getSimilarityWith(keyword) }
    }

    /**
     * it uses secondary maps service to cross validate the place from primary maps service.
     * @return true if the place does exist on the secondary maps service, false otherwise.
     */
    @Suppress("ReturnCount")
    suspend fun crossValidate(
        place: Place,
        option: MapsService.SearchByKeywordOption,
    ): Boolean {
        if (secondaryMapsService == null) {
            logger.error { "Secondary maps service is not available. Can not run cross validation" }
            return true
        }

        val keyword = "${place.address.siGunGu} ${place.address.eupMyeonDong} ${place.name}"
        val result = secondaryMapsService.findFirstByKeyword(keyword, option) ?: return false

        /**
         * this is very heuristic way to validate that addresses of two places are same.
         * it might not be accurate and need to check more fields of building address.
         *
         * the reason why siDo name is not checked is kakao returns "경기" but naver returns
         * "경기도" for the same address.
         */
        return place.address.siGunGu == result.address.siGunGu
            && place.address.eupMyeonDong == result.address.eupMyeonDong
            && place.address.mainBuildingNumber == result.address.mainBuildingNumber
    }

    fun setIsClosed(placeId: String, isClosed: Boolean) {
        val place = placeRepository.findById(placeId).get()
        place.setIsClosed(isClosed)
        placeRepository.save(place)
    }

    fun setIsNotAccessible(placeId: String, isNotAccessible: Boolean) {
        val place = placeRepository.findById(placeId).get()
        place.setIsNotAccessible(isNotAccessible)
        placeRepository.save(place)
    }

    fun searchPlacesInCircle(centerLocation: Location, radiusMeters: Int): List<Place> {
        // 현재 hibernate 6.2.0 미만 버전에서 native query 실행 시 eager loading이 안 되는 문제가 있다.
        // 따라서 native query로는 place id 목록만 얻어오고, place 자체는 별도로 조회해온다.
        val placeIds = placeRepository.findIdsByPlacesInCircle(centerLocation.lng, centerLocation.lat, radiusMeters.toDouble())
        return placeRepository.findAllByIdIn(placeIds)
    }

    fun searchPlacesInPolygon(points: List<Location>): List<Place> {
        val polygonWkt = WellKnownTextUtils.convertToPolygonWkt(points)
        // 현재 hibernate 6.2.0 미만 버전에서 native query 실행 시 eager loading이 안 되는 문제가 있다.
        // 따라서 native query로는 place id 목록만 얻어오고, place 자체는 별도로 조회해온다.
        val placeIds = placeRepository.findIdsByPlacesInPolygon(polygonWkt)
        return placeRepository.findAllByIdIn(placeIds)
    }

    fun isFavoritePlace(placeId: String, userId: String): Boolean {
        return placeFavoriteRepository.findFirstByUserIdAndPlaceIdAndDeletedAtIsNull(
            placeId = placeId,
            userId = userId
        ) != null
    }

    fun isFavoritePlaces(placeIds: Collection<String>, userId: String): Map<String, Boolean> {
        val favorites = placeFavoriteRepository.findAllByUserIdAndPlaceIdIsInAndDeletedAtIsNull(userId, placeIds)
            .associateBy { it.placeId }
        return placeIds.associateWith { placeId -> (favorites[placeId] != null) }
    }

    fun getTotalFavoriteCount(placeId: String): Long {
        return placeFavoriteRepository.countByPlaceIdAndDeletedAtIsNull(placeId)
    }

    private fun List<Place>.mergeLocalDatabases(): List<Place> {
        val existingPlaceById = placeRepository.findAllById(this.map { it.id })
            .associateBy { it.id }
        return this.map { searchedPlace ->
            existingPlaceById[searchedPlace.id]?.also { existingPlace ->
                Place.of(
                    id = searchedPlace.id,
                    name = searchedPlace.name,
                    location = searchedPlace.location,
                    building = searchedPlace.building,
                    siGunGuId = searchedPlace.siGunGuId,
                    eupMyeonDongId = searchedPlace.eupMyeonDongId,
                    category = searchedPlace.category,
                    isClosed = existingPlace.isClosed,
                    isNotAccessible = existingPlace.isNotAccessible,
                )
            } ?: searchedPlace
        }
    }

    companion object {
        private const val MIN_KEYWORD_LENGTH = 3
    }
}
