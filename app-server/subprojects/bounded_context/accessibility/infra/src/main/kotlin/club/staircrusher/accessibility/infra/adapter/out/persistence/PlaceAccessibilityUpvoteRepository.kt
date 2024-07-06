package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityUpvoteRepository
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityUpvote
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class PlaceAccessibilityUpvoteRepository(
    db: DB,
) : PlaceAccessibilityUpvoteRepository {
    private val queries = db.placeAccessibilityUpvoteQueries

    override fun save(entity: PlaceAccessibilityUpvote): PlaceAccessibilityUpvote {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<PlaceAccessibilityUpvote>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): PlaceAccessibilityUpvote {
        return findByIdOrNull(id)
            ?: throw IllegalArgumentException("BuildingAccessibilityComment of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): PlaceAccessibilityUpvote? {
        return queries.findUpvoteById(id = id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findUpvote(userId: String, placeAccessibilityId: String): PlaceAccessibilityUpvote? {
        return queries.findUpvoteByUserIdAndPlaceAccessilbityIdAndNotDeleted(userId, placeAccessibilityId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun countUpvotes(placeAccessibilityId: String): Int {
        return 0
    }
}
