package club.staircrusher.place.domain.model.accessibility

import club.staircrusher.stdlib.persistence.jpa.IntListToTextAttributeConverter
import club.staircrusher.stdlib.persistence.jpa.StringListToTextAttributeConverter
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import java.time.Instant

@Entity
class PlaceAccessibility(
    @Id
    val id: String,
    val placeId: String,
    @Convert(converter = IntListToTextAttributeConverter::class)
    var floors: List<Int>?,
    var isFirstFloor: Boolean,
    var isStairOnlyOption: Boolean?,
    @Enumerated(EnumType.STRING)
    var stairInfo: StairInfo,
    @Enumerated(EnumType.STRING)
    var stairHeightLevel: StairHeightLevel?,
    var hasSlope: Boolean,
    @Convert(converter = EntranceDoorTypeListToTextAttributeConverter::class)
    var entranceDoorTypes: List<EntranceDoorType>?,
    imageUrls: List<String>,
    images: List<AccessibilityImage>,
    val userId: String?,
    val createdAt: Instant,
    val deletedAt: Instant? = null,
) {
    @Deprecated("use images instead")
    @Convert(converter = StringListToTextAttributeConverter::class)
    var imageUrls: List<String> = imageUrls
        protected set

    @Convert(converter = AccessibilityImageListToTextAttributeConverter::class)
    var images: List<AccessibilityImage> = images
        protected set

    fun updateImages(images: List<AccessibilityImage>) {
        this.images = images
        this.imageUrls = images.map { it.imageUrl }
    }

    fun isDeletable(uid: String?): Boolean {
        return uid != null && uid == userId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceAccessibility

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "PlaceAccessibility(id='$id', placeId='$placeId', floors=$floors, isFirstFloor=$isFirstFloor, " +
            "isStairOnlyOption=$isStairOnlyOption, stairInfo=$stairInfo, stairHeightLevel=$stairHeightLevel, " +
            "hasSlope=$hasSlope, entranceDoorTypes=$entranceDoorTypes, imageUrls=$imageUrls, images=$images, " +
            "userId=$userId, createdAt=$createdAt, deletedAt=$deletedAt)"
    }
}
