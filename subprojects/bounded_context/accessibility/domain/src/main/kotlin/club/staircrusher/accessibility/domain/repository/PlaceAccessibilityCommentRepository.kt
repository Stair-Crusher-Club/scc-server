package club.staircrusher.accessibility.domain.repository

import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface PlaceAccessibilityCommentRepository : EntityRepository<PlaceAccessibilityComment, String> {
    fun findByPlaceId(placeId: String): List<PlaceAccessibilityComment>
}
