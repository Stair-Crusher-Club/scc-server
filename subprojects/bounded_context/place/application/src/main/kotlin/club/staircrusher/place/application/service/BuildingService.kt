package club.staircrusher.place.application.service

import club.staircrusher.place.application.port.out.persistence.BuildingRepository
import club.staircrusher.place.domain.model.Building
import club.staircrusher.stdlib.di.annotation.Component

@Component
class BuildingService(
    private val buildingRepository: BuildingRepository,
) {
    fun getById(buildingId: String): Building? {
        return buildingRepository.findByIdOrNull(buildingId)
    }
}
