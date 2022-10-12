package club.staircrusher.place_search.infra.adapter.out.web

import club.staircrusher.place_search.application.port.out.web.BuildingService
import club.staircrusher.place_search.domain.model.Building
import club.staircrusher.stdlib.di.annotation.Component

@Component
class InMemoryBuildingService(
    private val buildingService: club.staircrusher.place.application.service.BuildingService,
) : BuildingService {
    override fun getById(buildingId: String): Building? {
       return buildingService.getById(buildingId)?.let {
           Building(
               id = it.id,
               address = it.address.toString(),
           )
       }
    }
}
