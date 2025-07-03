package club.staircrusher.place.domain.model.accessibility.place_review

import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import club.staircrusher.user.domain.model.UserMobilityTool
import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import org.hibernate.annotations.Type
import org.hibernate.annotations.Where

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

    @OneToMany(mappedBy = "accessibilityId", fetch = FetchType.EAGER)
    @Where(clause = "accessibility_type = 'PlaceReview'")
    @OrderBy("displayOrder asc")
    val images: List<AccessibilityImage>,

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
) : TimeAuditingBaseEntity()
