package club.staircrusher.place.domain.model.accessibility

import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "accessibility_image")
class AccessibilityImage(
    @Id
    val id: String,

    @Column(nullable = false)
    val accessibilityId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val accessibilityType: AccessibilityType,

    @Column(nullable = false, name = "image_url")
    val originalImageUrl: String,

    @Column(nullable = true)
    var blurredImageUrl: String? = null,

    @Column(nullable = true)
    var thumbnailUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var imageType: ImageType? = null,

    @Column(nullable = true)
    var lastPostProcessedAt: Instant? = null,

    @Column(nullable = true)
    var displayOrder: Int? = null,
) : TimeAuditingBaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccessibilityImage

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Image(id='$id', imageUrl='$originalImageUrl', thumbnailUrl=$thumbnailUrl, " +
            "accessibilityId=$accessibilityId, " +
            "imageType=$imageType)"
    }

    enum class ImageType {
        Entrance, Elevator
    }

    enum class AccessibilityType {
        Place, Building, PlaceReview
    }
}
