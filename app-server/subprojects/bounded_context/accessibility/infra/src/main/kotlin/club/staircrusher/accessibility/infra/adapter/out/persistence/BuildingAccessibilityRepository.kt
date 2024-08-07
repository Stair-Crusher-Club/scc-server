package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.EupMyeonDong
import club.staircrusher.stdlib.time.toOffsetDateTime
import java.time.Instant

@Suppress("TooManyFunctions")
@Component
class BuildingAccessibilityRepository(
    db: DB,
) : BuildingAccessibilityRepository {
    private val queries = db.buildingAccessibilityQueries

    override fun save(entity: BuildingAccessibility): BuildingAccessibility {
        queries.save(entity.toPersistenceModel().copy(updated_at = SccClock.instant().toOffsetDateTime()))
        return entity
    }

    override fun saveAll(entities: Collection<BuildingAccessibility>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): BuildingAccessibility {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("BuildingAccessibility of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): BuildingAccessibility? {
        return queries.findById(id = id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByBuildingIds(buildingIds: Collection<String>): List<BuildingAccessibility> {
        if (buildingIds.isEmpty()) {
            return emptyList()
        }
        return queries.findByBuildingIds(buildingIds = buildingIds)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByBuildingId(buildingId: String): BuildingAccessibility? {
        return queries.findByBuildingId(buildingId = buildingId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByPlaceIds(placeIds: Collection<String>): List<BuildingAccessibility> {
        return queries.findByPlaceIds(placeIds = placeIds)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByUserIdAndCreatedAtBetween(userId: String, from: Instant, to: Instant): List<BuildingAccessibility> {
        return queries.findByUserIdAndCreatedAtBetween(
            userId = userId,
            from = from.toOffsetDateTime(),
            to = to.toOffsetDateTime()
        )
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByEupMyeonDong(eupMyeonDong: EupMyeonDong): List<BuildingAccessibility> {
        return queries.findByEupMyeonDong(eupMyeonDongId = eupMyeonDong.id)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun updateEntranceImages(id: String, entranceImages: List<AccessibilityImage>) {
        return queries.updateEntranceImages(
            entranceImages = entranceImages,
            id = id,
        )
    }

    override fun updateElevatorImages(id: String, elevatorImages: List<AccessibilityImage>) {
        return queries.updateElevatorImages(
            elevatorImages = elevatorImages,
            id = id,
        )
    }

    override fun countByUserId(userId: String): Int {
        return queries.countByUserId(userId = userId).executeAsOne().toInt()
    }

    override fun countByUserIdCreatedAtBetween(userId: String, from: Instant, to: Instant): Int {
        return queries.countByUserIdAndCreatedAtBetween(
            userId = userId,
            from = from.toOffsetDateTime(),
            to = to.toOffsetDateTime()
        ).executeAsOne().toInt()
    }

    override fun remove(id: String) {
        queries.remove(SccClock.instant().toOffsetDateTime(), id)
    }
}
