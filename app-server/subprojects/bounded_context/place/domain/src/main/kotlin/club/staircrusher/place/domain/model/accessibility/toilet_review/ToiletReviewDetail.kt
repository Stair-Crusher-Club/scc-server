package club.staircrusher.place.domain.model.accessibility.toilet_review

import club.staircrusher.place.domain.model.accessibility.EntranceDoorType
import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.annotations.Type

@Embeddable
data class ToiletReviewDetail(
    val floor: Int,

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    val entranceDoorTypes: List<EntranceDoorType>,

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    val imageUrls: List<String>,

    @Column(columnDefinition = "text")
    val comment: String,
)
