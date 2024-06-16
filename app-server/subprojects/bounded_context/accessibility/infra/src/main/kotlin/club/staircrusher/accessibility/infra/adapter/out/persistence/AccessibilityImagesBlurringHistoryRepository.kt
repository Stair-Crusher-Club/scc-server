package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImagesBlurringHistoryRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImagesBlurringHistory
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.time.toOffsetDateTime

@Suppress("TooManyFunctions")
@Component
class AccessibilityImagesBlurringHistoryRepository(
    db: DB,
) : AccessibilityImagesBlurringHistoryRepository {
    private val queries = db.accessibilityImagesBlurringHistoryQueries
    override fun findLatestHistoryOrNull(): AccessibilityImagesBlurringHistory? {
        return queries.findLatestHistory().executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByPlaceAccessibilityId(placeAccessibilityId: String): List<AccessibilityImagesBlurringHistory> {
        return queries.findByPlaceAccessibility(placeAccessibilityId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByBuildingAccessibilityId(buildingAccessibilityId: String): List<AccessibilityImagesBlurringHistory> {
        return queries.findByBuildingAccessilbility(buildingAccessibilityId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun save(entity: AccessibilityImagesBlurringHistory): AccessibilityImagesBlurringHistory {
        queries.save(
            entity.toPersistenceModel()
                .copy(updated_at = SccClock.instant().toOffsetDateTime())
        )
        return entity
    }

    override fun saveAll(entities: Collection<AccessibilityImagesBlurringHistory>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): AccessibilityImagesBlurringHistory {
        return findByIdOrNull(id)
            ?: throw IllegalArgumentException("AccessibilityImagesBlurringHistory of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): AccessibilityImagesBlurringHistory? {
        return queries.findById(id = id).executeAsOneOrNull()?.toDomainModel()
    }
}
