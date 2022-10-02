package club.staircrusher.place.infra.adapter.out.persistence

import club.staircrusher.place.application.port.out.persistence.BuildingRepository
import club.staircrusher.place.domain.model.Building
import club.staircrusher.stdlib.geography.EupMyeonDong
import org.springframework.stereotype.Component

@Component
class NoopBuildingRepository : BuildingRepository {
    override fun findByIdIn(ids: Collection<String>): List<Building> {
        return emptyList()
    }

    override fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int {
        return 0
    }

    override fun save(entity: Building): Building {
        return entity
    }

    override fun saveAll(entity: Collection<Building>): Building {
        return entity.first()
    }

    override fun removeAll() {
        // No-op
    }

    override fun findById(id: String): Building {
        throw IllegalArgumentException("Building of id $id does not exists")
    }

    override fun findByIdOrNull(id: String): Building? {
        return null
    }
}
