package club.staircrusher.place.application.port.`in`

import club.staircrusher.domain_event.PlaceSearchEvent
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import club.staircrusher.stdlib.place.PlaceCategory

@Component
class PlaceService(
    private val placeRepository: PlaceRepository,
    private val eventPublisher: DomainEventPublisher,
    private val mapsServices: List<MapsService>,
) {
    private val mapsService = mapsServices.first()
    private val secondaryMapsService = mapsServices.getOrNull(1)

    fun findPlace(placeId: String): Place? {
        return placeRepository.findByIdOrNull(placeId)
    }

    suspend fun findAllByKeyword(keyword: String, option: MapsService.SearchByKeywordOption): List<Place> {
        if (keyword.isBlank()) {
            return emptyList()
        }
        var places = mapsService.findAllByKeyword(keyword, option)
        if (places.isEmpty() && secondaryMapsService != null) {
            places = secondaryMapsService.findAllByKeyword(keyword, option)
        }
        eventPublisher.publishEvent(PlaceSearchEvent(places.map(Place::toPlaceDTO)))
        return places
    }

    suspend fun findAllByCategory(
        category: PlaceCategory,
        option: MapsService.SearchByCategoryOption
    ): List<Place> {
        var places = mapsService.findAllByCategory(category, option)
        if (places.isEmpty() && secondaryMapsService != null) {
            places = secondaryMapsService.findAllByCategory(category, option)
        }
        eventPublisher.publishEvent(PlaceSearchEvent(places.map(Place::toPlaceDTO)))
        return places
    }

    fun findAllByIds(placeIds: Collection<String>): List<Place> {
        return placeRepository.findByIdIn(placeIds)
    }

    fun findByBuildingId(buildingId: String): List<Place> {
        return placeRepository.findByBuildingId(buildingId)
    }
}
