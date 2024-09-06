package club.staircrusher.place.application.port.`in`

import club.staircrusher.domain_event.PlaceSearchEvent
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.WellKnownTextUtils
import club.staircrusher.stdlib.place.PlaceCategory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull

@Component
class PlaceApplicationService(
    private val placeRepository: PlaceRepository,
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

    private fun List<Place>.mergeLocalDatabases(): List<Place> {
        val existingPlaceById = placeRepository.findAllById(this.map { it.id })
            .associateBy { it.id }
        return this.map {
            existingPlaceById[it.id] ?: it
        }
    }
}
