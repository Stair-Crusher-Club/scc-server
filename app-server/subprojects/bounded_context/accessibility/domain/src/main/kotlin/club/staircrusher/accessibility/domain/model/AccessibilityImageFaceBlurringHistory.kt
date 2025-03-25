package club.staircrusher.accessibility.domain.model

import club.staircrusher.stdlib.persistence.jpa.IntListToTextAttributeConverter
import club.staircrusher.stdlib.persistence.jpa.StringListToTextAttributeConverter
import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class AccessibilityImageFaceBlurringHistory(
    @Id
    val id: String,
    val placeAccessibilityId: String?,
    val buildingAccessibilityId: String?,
    @Convert(converter = StringListToTextAttributeConverter::class)
    val originalImageUrls: List<String>,
    @Convert(converter = StringListToTextAttributeConverter::class)
    val blurredImageUrls: List<String>,
    @Convert(converter = IntListToTextAttributeConverter::class)
    val detectedPeopleCounts: List<Int>,
) : TimeAuditingBaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccessibilityImageFaceBlurringHistory

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "AccessibilityImageFaceBlurringHistory(id='$id', placeAccessibilityId=$placeAccessibilityId, " +
            "buildingAccessibilityId=$buildingAccessibilityId, originalImageUrls=$originalImageUrls, " +
            "blurredImageUrls=$blurredImageUrls, detectedPeopleCounts=$detectedPeopleCounts, createdAt=$createdAt, " +
            "updatedAt=$updatedAt)"
    }


}
