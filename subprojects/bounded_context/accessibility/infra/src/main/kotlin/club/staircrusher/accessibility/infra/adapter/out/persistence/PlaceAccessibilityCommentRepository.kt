package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.repository.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class PlaceAccessibilityCommentRepository(
    db: DB,
) : PlaceAccessibilityCommentRepository {
    private val queries = db.placeAccessibilityCommentQueries

    override fun save(entity: PlaceAccessibilityComment): PlaceAccessibilityComment {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entity: Collection<PlaceAccessibilityComment>): PlaceAccessibilityComment {
        entity.forEach(::save)
        return entity.first()
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
}
