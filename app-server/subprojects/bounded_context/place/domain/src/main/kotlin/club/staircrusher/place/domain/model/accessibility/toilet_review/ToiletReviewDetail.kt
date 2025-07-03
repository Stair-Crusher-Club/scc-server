package club.staircrusher.place.domain.model.accessibility.toilet_review

import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.EntranceDoorType
import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import org.hibernate.annotations.Type
import org.hibernate.annotations.Where

@Embeddable
data class ToiletReviewDetail(
    val floor: Int,

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    val entranceDoorTypes: List<EntranceDoorType>,

    @OneToMany(mappedBy = "accessibilityId", fetch = FetchType.EAGER)
    @Where(clause = "accessibility_type = 'ToiletReview'")
    @OrderBy("displayOrder asc")
    val images: List<AccessibilityImage>,
)
