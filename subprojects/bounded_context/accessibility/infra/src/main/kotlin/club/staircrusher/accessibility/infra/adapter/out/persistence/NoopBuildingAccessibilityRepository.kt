package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityRepository
import club.staircrusher.stdlib.geography.EupMyeonDong
import club.staircrusher.stdlib.di.annotation.Component

@Component
class NoopBuildingAccessibilityRepository : BuildingAccessibilityRepository {
    override fun findByBuildingIds(buildingIds: Collection<String>): List<BuildingAccessibility> {
        return emptyList()
    }

    override fun findByBuildingId(buildingId: String): BuildingAccessibility? {
        return null
    }

    override fun findByPlaceIds(placeIds: Collection<String>): List<BuildingAccessibility> {
        return emptyList()
    }

    override fun findByEupMyeonDong(eupMyeonDong: EupMyeonDong): List<BuildingAccessibility> {
        return emptyList()
    }

    override fun countByUserId(userId: String): Int {
        return 0
    }

    override fun save(entity: BuildingAccessibility): BuildingAccessibility {
        return entity
    }

    override fun saveAll(entity: Collection<BuildingAccessibility>): BuildingAccessibility {
        return entity.first()
    }

    override fun removeAll() {
        // No-op
    }

    override fun findByIdOrNull(id: String): BuildingAccessibility? {
        return null
    }

    override fun findById(id: String): BuildingAccessibility {
        throw IllegalArgumentException("BuildingAccessibility of id $id does not exist.")
    }
}
