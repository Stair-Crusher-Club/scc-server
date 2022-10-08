package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.repository.PlaceAccessibilityRepository
import club.staircrusher.stdlib.geography.EupMyeonDong
import club.staircrusher.stdlib.di.annotation.Component

@Component
class NoopPlaceAccessibilityRepository : PlaceAccessibilityRepository {
    override fun findByPlaceIds(placeIds: Collection<String>): List<PlaceAccessibility> {
        return emptyList()
    }

    override fun findByPlaceId(placeId: String): PlaceAccessibility? {
        return null
    }

    override fun findByUserId(userId: String): List<PlaceAccessibility> {
        return emptyList()
    }

    override fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int {
        return 0
    }

    override fun countByUserId(userId: String): Int {
        return 0
    }

    override fun countByUserIdGroupByEupMyeonDongId(userId: String): Map<String, Int> {
        return emptyMap()
    }

    override fun hasAccessibilityNotRegisteredPlaceInBuilding(buildingId: String): Boolean {
        return false
    }

    override fun countAll(): Int {
        return 0
    }

    override fun listConquerRankingEntries(): List<Pair<String, Int>> {
        return emptyList()
    }

    override fun save(entity: PlaceAccessibility): PlaceAccessibility {
        return entity
    }

    override fun saveAll(entity: Collection<PlaceAccessibility>): PlaceAccessibility {
        return entity.first()
    }

    override fun removeAll() {
        // No-op
    }

    override fun findById(id: String): PlaceAccessibility {
        throw IllegalArgumentException("PlaceAccessibility of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): PlaceAccessibility? {
        return null
    }
}
