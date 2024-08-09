package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.time.toOffsetDateTime

@Suppress("TooManyFunctions")
@Component
class AccessibilityImageFaceBlurringHistoryRepository(
    db: DB,
) : AccessibilityImageFaceBlurringHistoryRepository {
    private val queries = db.accessibilityImageFaceBlurringHistoryQueries

    override fun findLatestPlaceHistoryOrNull(): AccessibilityImageFaceBlurringHistory? {
        return queries.findLatestPlaceHistory().executeAsOneOrNull()?.toDomainModel()
    }

    override fun findLatestBuildingHistoryOrNull(): AccessibilityImageFaceBlurringHistory? {
        return queries.findLatestBuildingHistory().executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByPlaceAccessibilityId(placeAccessibilityId: String): List<AccessibilityImageFaceBlurringHistory> {
        return queries.findByPlaceAccessibility(placeAccessibilityId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByBuildingAccessibilityId(buildingAccessibilityId: String): List<AccessibilityImageFaceBlurringHistory> {
        return queries.findByBuildingAccessilbility(buildingAccessibilityId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun save(entity: AccessibilityImageFaceBlurringHistory): AccessibilityImageFaceBlurringHistory {
        queries.save(
            entity.toPersistenceModel()
                .copy(updated_at = SccClock.instant().toOffsetDateTime())
        )
        return entity
    }

    override fun saveAll(entities: Collection<AccessibilityImageFaceBlurringHistory>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): AccessibilityImageFaceBlurringHistory {
        return findByIdOrNull(id)
            ?: throw IllegalArgumentException("AccessibilityImagesBlurringHistory of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): AccessibilityImageFaceBlurringHistory? {
        return queries.findById(id = id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findAll(): List<AccessibilityImageFaceBlurringHistory> {
        return queries.findAll().executeAsList().map { it.toDomainModel() }
    }
}
