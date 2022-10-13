package club.staircrusher.place.application.port.`in`

import club.staircrusher.domain_event.PlaceSearchEvent
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.application.toPlaceDTO
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import club.staircrusher.stdlib.place.PlaceCategory

@Component
class PlaceService(
    private val placeRepository: PlaceRepository,
    private val eventPublisher: DomainEventPublisher,
    private val mapsService: MapsService,
) {
    fun findPlace(placeId: String): Place? {
        return placeRepository.findByIdOrNull(placeId)
    }

    // TODO: support filter
    suspend fun findByKeyword(keyword: String): List<Place> {
        val places = mapsService.findByKeyword(keyword)
        eventPublisher.publishEvent(PlaceSearchEvent(places.map(Place::toPlaceDTO)))
        return places
    }

    suspend fun findAllByKeyword(keyword: String): List<Place> {
        val places = mapsService.findAllByKeyword(keyword)
        eventPublisher.publishEvent(PlaceSearchEvent(places.map(Place::toPlaceDTO)))
        return places
    }

    suspend fun findAllByCategory(
        category: PlaceCategory,
        option: MapsService.SearchOption
    ): List<Place> {
        val places = mapsService.findAllByCategory(category, option)
        eventPublisher.publishEvent(PlaceSearchEvent(places.map(Place::toPlaceDTO)))
        return places
    }

    fun findAllByIds(placeIds: Collection<String>): List<Place> {
        return placeRepository.findByIdIn(placeIds)
    }
}
