package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.repository.PlaceAccessibilityCommentRepository
import club.staircrusher.stdlib.di.annotation.Component

@Component
class NoopPlaceAccessibilityCommentRepository : PlaceAccessibilityCommentRepository {
    override fun findByPlaceId(placeId: String): List<PlaceAccessibilityComment> {
        return emptyList()
    }

    override fun save(entity: PlaceAccessibilityComment): PlaceAccessibilityComment {
        return entity
    }

    override fun saveAll(entity: Collection<PlaceAccessibilityComment>): PlaceAccessibilityComment {
        return entity.first()
    }

    override fun removeAll() {
        // No-op
    }

    override fun findById(id: String): PlaceAccessibilityComment {
        throw IllegalArgumentException("PlaceAccessibilityComment of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): PlaceAccessibilityComment? {
        return null
    }
}
