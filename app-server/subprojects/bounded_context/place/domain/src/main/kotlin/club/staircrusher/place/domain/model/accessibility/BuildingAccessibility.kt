package club.staircrusher.place.domain.model.accessibility

import club.staircrusher.stdlib.persistence.jpa.StringListToTextAttributeConverter
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
class BuildingAccessibility(
    @Id
    val id: String,
    val buildingId: String,
    @Enumerated(EnumType.STRING)
    var entranceStairInfo: StairInfo,
    @Enumerated(EnumType.STRING)
    var entranceStairHeightLevel: StairHeightLevel?,
    entranceImageUrls: List<String>,
    entranceImages: List<AccessibilityImageOld>,
    var hasSlope: Boolean,
    var hasElevator: Boolean,
    @Convert(converter = EntranceDoorTypeListToTextAttributeConverter::class)
    var entranceDoorTypes: List<EntranceDoorType>?,
    @Enumerated(EnumType.STRING)
    var elevatorStairInfo: StairInfo,
    @Enumerated(EnumType.STRING)
    var elevatorStairHeightLevel: StairHeightLevel?,
    elevatorImageUrls: List<String>,
    elevatorImages: List<AccessibilityImageOld>,
    val userId: String?,
    val createdAt: Instant,
    val deletedAt: Instant? = null,

    @OneToMany(mappedBy = "accessibilityId", fetch = FetchType.EAGER)
    @Where(clause = "accessibility_type = 'Building' and image_type = 'Elevator'")
    var newElevatorAccessibilityImages: MutableList<AccessibilityImage> = mutableListOf(),

    @OneToMany(mappedBy = "accessibilityId", fetch = FetchType.EAGER)
    @Where(clause = "accessibility_type = 'Building' and image_type = 'Entrance'")
    var newEntranceAccessibilityImages: MutableList<AccessibilityImage> = mutableListOf(),
) {
    @Convert(converter = AccessibilityImageListToTextAttributeConverter::class)
    var entranceImages: List<AccessibilityImageOld> = entranceImages

    @Deprecated("use images with type instead")
    @Convert(converter = StringListToTextAttributeConverter::class)
    var entranceImageUrls: List<String> = entranceImageUrls

    @Convert(converter = AccessibilityImageListToTextAttributeConverter::class)
    var elevatorImages: List<AccessibilityImageOld> = elevatorImages

    @Deprecated("use images with type instead")
    @Convert(converter = StringListToTextAttributeConverter::class)
    var elevatorImageUrls: List<String> = elevatorImageUrls

    fun isDeletable(uid: String?): Boolean {
        return uid != null && uid == userId
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
            "entranceStairHeightLevel=$entranceStairHeightLevel, " +
            "hasSlope=$hasSlope, hasElevator=$hasElevator, " +
            "entranceDoorTypes=$entranceDoorTypes, elevatorStairInfo=$elevatorStairInfo, " +
            "elevatorStairHeightLevel=$elevatorStairHeightLevel, " +
            "userId=$userId, createdAt=$createdAt, deletedAt=$deletedAt)"
    }
}
