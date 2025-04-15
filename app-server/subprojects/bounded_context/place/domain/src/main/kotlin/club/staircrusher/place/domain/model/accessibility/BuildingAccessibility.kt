package club.staircrusher.place.domain.model.accessibility

import club.staircrusher.stdlib.persistence.jpa.StringListToTextAttributeConverter
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import java.time.Instant

@Entity
class BuildingAccessibility(
    @Id
    val id: String,
    val buildingId: String,
    @Enumerated(EnumType.STRING)
    var entranceStairInfo: StairInfo,
    @Enumerated(EnumType.STRING)
    var entranceStairHeightLevel: StairHeightLevel?,
    entranceImageUrls: List<String>,
    entranceImages: List<AccessibilityImage>,
    var hasSlope: Boolean,
    var hasElevator: Boolean,
    @Convert(converter = EntranceDoorTypeListToTextAttributeConverter::class)
    var entranceDoorTypes: List<EntranceDoorType>?,
    @Enumerated(EnumType.STRING)
    var elevatorStairInfo: StairInfo,
    @Enumerated(EnumType.STRING)
    var elevatorStairHeightLevel: StairHeightLevel?,
    elevatorImageUrls: List<String>,
    elevatorImages: List<AccessibilityImage>,
    val userId: String?,
    val createdAt: Instant,
    val deletedAt: Instant? = null,
) {
    @Convert(converter = AccessibilityImageListToTextAttributeConverter::class)
    var entranceImages: List<AccessibilityImage> = entranceImages
        protected set

    @Deprecated("use images with type instead")
    @Convert(converter = StringListToTextAttributeConverter::class)
    var entranceImageUrls: List<String> = entranceImageUrls
        protected set

    @Convert(converter = AccessibilityImageListToTextAttributeConverter::class)
    var elevatorImages: List<AccessibilityImage> = elevatorImages
        protected set

    @Deprecated("use images with type instead")
    @Convert(converter = StringListToTextAttributeConverter::class)
    var elevatorImageUrls: List<String> = elevatorImageUrls
        protected set

    fun updateEntranceImages(images: List<AccessibilityImage>) {
        this.entranceImages = images
        this.entranceImageUrls = images.map { it.imageUrl }
    }

    fun updateElevatorImages(images: List<AccessibilityImage>) {
        this.elevatorImages = images
        this.elevatorImageUrls = images.map { it.imageUrl }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BuildingAccessibility

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "BuildingAccessibility(id='$id', buildingId='$buildingId', entranceStairInfo=$entranceStairInfo, " +
            "entranceStairHeightLevel=$entranceStairHeightLevel, entranceImageUrls=$entranceImageUrls, " +
            "entranceImages=$entranceImages, hasSlope=$hasSlope, hasElevator=$hasElevator, " +
            "entranceDoorTypes=$entranceDoorTypes, elevatorStairInfo=$elevatorStairInfo, " +
            "elevatorStairHeightLevel=$elevatorStairHeightLevel, elevatorImageUrls=$elevatorImageUrls, " +
            "elevatorImages=$elevatorImages, userId=$userId, createdAt=$createdAt, deletedAt=$deletedAt)"
    }
}
