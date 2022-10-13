package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.EupMyeonDong

@Suppress("TooManyFunctions")
@Component
class PlaceAccessibilityRepository(
    db: DB,
) : PlaceAccessibilityRepository {
    private val queries = db.placeAccessibilityQueries

    override fun save(entity: PlaceAccessibility): PlaceAccessibility {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entity: Collection<PlaceAccessibility>): PlaceAccessibility {
        entity.forEach(::save)
        return entity.first()
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): PlaceAccessibility {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("PlaceAccessibility of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): PlaceAccessibility? {
        return queries.findById(id = id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByPlaceIds(placeIds: Collection<String>): List<PlaceAccessibility> {
        return queries.findByPlaceIds(placeIds = placeIds)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByPlaceId(placeId: String): PlaceAccessibility? {
        return queries.findByPlaceId(placeId = placeId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByUserId(userId: String): List<PlaceAccessibility> {
        return queries.findByUserId(userId = userId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int {
        return queries.countByEupMyeonDong(eupMyeonDongId = eupMyeonDong.id)
            .executeAsOne()
            .toInt()
    }

    override fun countByUserId(userId: String): Int {
        return queries.countByUserId(userId = userId).executeAsOne().toInt()
    }

    override fun hasAccessibilityNotRegisteredPlaceInBuilding(buildingId: String): Boolean {
        return queries.hasAccessibilityNotRegisteredPlaceInBuilding(buildingId = buildingId)
            .executeAsOne()
    }

    override fun countAll(): Int {
        return queries.countAll()
            .executeAsOne()
            .toInt()
    }
}
