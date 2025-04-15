package club.staircrusher.place.application.port.`in`.place

import club.staircrusher.place.application.port.out.place.persistence.BuildingRepository
import club.staircrusher.place.domain.model.place.Building
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.data.repository.findByIdOrNull

@Component
class BuildingService(
    private val buildingRepository: BuildingRepository,
) {
    fun getById(buildingId: String): Building? {
        return buildingRepository.findByIdOrNull(buildingId)
    }
}
