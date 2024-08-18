package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import org.springframework.data.repository.CrudRepository

interface PlaceAccessibilityCommentRepository : CrudRepository<PlaceAccessibilityComment, String> {
    fun findByPlaceId(placeId: String): List<PlaceAccessibilityComment>
    fun removeByPlaceId(placeId: String)
    data class CreateParams(
        val placeId: String,
        val userId: String?,
        val comment: String,
    )
}
