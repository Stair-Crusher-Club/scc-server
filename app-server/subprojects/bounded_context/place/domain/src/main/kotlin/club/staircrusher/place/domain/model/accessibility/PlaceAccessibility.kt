package club.staircrusher.place.domain.model.accessibility

import club.staircrusher.stdlib.persistence.jpa.IntListToTextAttributeConverter
import club.staircrusher.stdlib.persistence.jpa.StringListToTextAttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.hibernate.annotations.Where
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
    oldImageUrls: List<String>,
    oldImages: List<AccessibilityImageOld>,
    val userId: String?,
    val createdAt: Instant,
    val deletedAt: Instant? = null,

    @OneToMany(mappedBy = "accessibilityId", fetch = FetchType.EAGER)
    @Where(clause = "accessibility_type = 'Place'")
    var images: MutableList<AccessibilityImage> = mutableListOf(),
) {
    @Deprecated("use images instead")
    @Convert(converter = StringListToTextAttributeConverter::class)
    @Column(name = "image_urls")
    var oldImageUrls: List<String> = oldImageUrls

    @Convert(converter = AccessibilityImageListToTextAttributeConverter::class)
    @Column(name = "images")
    var oldImages: List<AccessibilityImageOld> = oldImages

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
            "hasSlope=$hasSlope, entranceDoorTypes=$entranceDoorTypes, " +
            "userId=$userId, createdAt=$createdAt, deletedAt=$deletedAt)"
    }
}
