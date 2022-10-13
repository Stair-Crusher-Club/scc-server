package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface PlaceAccessibilityCommentRepository : EntityRepository<PlaceAccessibilityComment, String> {
    fun findByPlaceId(placeId: String): List<PlaceAccessibilityComment>
    data class CreateParams(
        val placeId: String,
        val userId: String?,
        val comment: String,
    )
}
