package club.staircrusher.place.domain.model.accessibility.place_review

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.user.domain.model.UserMobilityTool
import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import org.hibernate.annotations.Type
import java.time.Instant

@Entity
class PlaceReview(
    @Id
    val id: String = EntityIdGenerator.generateRandom(),

    val placeId: String,

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    val recommendedMobilityTypes: List<PlaceReviewRecommendedMobilityType>,

    @Enumerated(EnumType.STRING)
    val spaciousType: PlaceReviewSpaciousType,

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    val imageUrls: List<String>,

    val comment: String,

    @Enumerated(EnumType.STRING)
    val mobilityTool: UserMobilityTool,

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    val seatTypes: List<String>,

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    val orderMethods: List<String>,

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    val features: List<String>,
) {
    val createdAt: Instant = SccClock.instant()
}
