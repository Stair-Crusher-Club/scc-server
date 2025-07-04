package club.staircrusher.place.application.port.out.accessibility.persistence.toilet_review

import club.staircrusher.place.domain.model.accessibility.EntranceDoorType
import club.staircrusher.place.domain.model.accessibility.toilet_review.ToiletLocationType
import club.staircrusher.place.domain.model.accessibility.toilet_review.ToiletReview
import org.springframework.data.repository.CrudRepository

interface ToiletReviewRepository : CrudRepository<ToiletReview, String> {
    fun findAllByTargetIdOrderByCreatedAtDesc(targetId: String): List<ToiletReview>

    data class CreateParams(
        val placeId: String,
        val userId: String,
        val toiletLocationType: ToiletLocationType,
        val floor: Int?,
        val entranceDoorTypes: List<EntranceDoorType>,
        val imageUrls: List<String>,
        val comment: String?,
    )
}
