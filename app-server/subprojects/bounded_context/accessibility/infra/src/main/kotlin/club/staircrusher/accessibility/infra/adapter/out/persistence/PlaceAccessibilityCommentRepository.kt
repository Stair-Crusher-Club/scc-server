package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.time.toOffsetDateTime

@Component
class PlaceAccessibilityCommentRepository(
    db: DB,
) : PlaceAccessibilityCommentRepository {
    private val queries = db.placeAccessibilityCommentQueries

    override fun save(entity: PlaceAccessibilityComment): PlaceAccessibilityComment {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<PlaceAccessibilityComment>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): PlaceAccessibilityComment {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("PlaceAccessibilityComment of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): PlaceAccessibilityComment? {
        return queries.findById(id = id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByPlaceId(placeId: String): List<PlaceAccessibilityComment> {
        return queries.findByPlaceId(placeId = placeId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun removeByPlaceId(placeId: String) {
        queries.removeByPlaceId(SccClock.instant().toOffsetDateTime(), placeId)
    }
}
