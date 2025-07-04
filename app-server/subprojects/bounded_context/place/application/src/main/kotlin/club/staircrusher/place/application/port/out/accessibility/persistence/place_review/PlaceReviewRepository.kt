package club.staircrusher.place.application.port.out.accessibility.persistence.place_review

import club.staircrusher.place.domain.model.accessibility.place_review.PlaceReview
import club.staircrusher.place.domain.model.accessibility.place_review.PlaceReviewRecommendedMobilityType
import club.staircrusher.place.domain.model.accessibility.place_review.PlaceReviewSpaciousType
import club.staircrusher.user.domain.model.UserMobilityTool
import org.springframework.data.repository.CrudRepository

interface PlaceReviewRepository : CrudRepository<PlaceReview, String> {

    data class CreateParams(
        val placeId: String,
        val userId: String,
        val recommendedMobilityTypes: List<PlaceReviewRecommendedMobilityType>,
        val spaciousType: PlaceReviewSpaciousType,
        val imageUrls: List<String>,
        val comment: String?,
        val mobilityTool: UserMobilityTool,
        val seatTypes: List<String>,
        val orderMethods: List<String>,
        val features: List<String>,
    )
}
