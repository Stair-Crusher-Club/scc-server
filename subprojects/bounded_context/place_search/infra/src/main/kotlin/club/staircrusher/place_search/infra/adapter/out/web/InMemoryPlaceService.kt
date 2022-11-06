package club.staircrusher.place_search.infra.adapter.out.web

import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place_search.application.port.out.web.PlaceService
import club.staircrusher.place_search.domain.model.Building
import club.staircrusher.place_search.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component

@Component
class InMemoryPlaceService(
    private val placeService: club.staircrusher.place.application.port.`in`.PlaceService,
    private val placeRepository: PlaceRepository,
) : PlaceService {
    override suspend fun findAllByKeyword(keyword: String, option: MapsService.SearchByKeywordOption): List<Place> {
        // Since `Place` class of place domain is not included in application layer,
        // it is not easy to define extension function for converting from place.Place
        // to place_search.Place. Would it be better to define data transfer object in
        // application layer so that other source set depending on it can define extension
        // method easily?
        return placeService.findAllByKeyword(keyword, option)
            .map { it.toModel() }
    }

    override fun findAllByIds(ids: Collection<String>): List<Place> {
        return placeService.findAllByIds(ids).map { it.toModel() }
    }

    override fun findByBuildingId(buildingId: String): List<Place> {
        return placeRepository.findByBuildingId(buildingId).map { it.toModel() }
    }

    private fun club.staircrusher.place.domain.model.Place.toModel() = Place(
        id = id,
        name = name,
        address = address.toString(),
        location = location,
        building = Building(
            id = building!!.id,
            address = building!!.address.toString(),
        ),
    )
}
