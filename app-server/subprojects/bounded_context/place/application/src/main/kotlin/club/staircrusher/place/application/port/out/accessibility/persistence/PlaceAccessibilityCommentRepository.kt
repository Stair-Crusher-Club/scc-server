package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.PlaceAccessibilityComment
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
