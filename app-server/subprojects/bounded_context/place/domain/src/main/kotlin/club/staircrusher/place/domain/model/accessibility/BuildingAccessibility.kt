package club.staircrusher.place.domain.model.accessibility

import club.staircrusher.stdlib.persistence.jpa.StringListToTextAttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
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
    oldEntranceImageUrls: List<String>,
    oldEntranceImages: List<AccessibilityImageOld>,
    var hasSlope: Boolean,
    var hasElevator: Boolean,
    @Convert(converter = EntranceDoorTypeListToTextAttributeConverter::class)
    var entranceDoorTypes: List<EntranceDoorType>?,
    @Enumerated(EnumType.STRING)
    var elevatorStairInfo: StairInfo,
    @Enumerated(EnumType.STRING)
    var elevatorStairHeightLevel: StairHeightLevel?,
    oldElevatorImageUrls: List<String>,
    oldElevatorImages: List<AccessibilityImageOld>,
    val userId: String?,
    val createdAt: Instant,
    val deletedAt: Instant? = null,

    @OneToMany(mappedBy = "accessibilityId", fetch = FetchType.EAGER)
    @Where(clause = "accessibility_type = 'Building' and image_type = 'Elevator'")
    @OrderBy("displayOrder asc")
    var elevatorImages: MutableList<AccessibilityImage> = mutableListOf(),

    @OneToMany(mappedBy = "accessibilityId", fetch = FetchType.EAGER)
    @Where(clause = "accessibility_type = 'Building' and image_type = 'Entrance'")
    @OrderBy("displayOrder asc")
    var entranceImages: MutableList<AccessibilityImage> = mutableListOf(),
) {
    @Convert(converter = AccessibilityImageListToTextAttributeConverter::class)
    @Column(name = "entrance_images")
    var oldEntranceImages: List<AccessibilityImageOld> = oldEntranceImages

    @Deprecated("use images with type instead")
    @Convert(converter = StringListToTextAttributeConverter::class)
    @Column(name = "entrance_image_urls")
    var oldEntranceImageUrls: List<String> = oldEntranceImageUrls

    @Convert(converter = AccessibilityImageListToTextAttributeConverter::class)
    @Column(name = "elevator_images")
    var oldElevatorImages: List<AccessibilityImageOld> = oldElevatorImages

    @Deprecated("use images with type instead")
    @Convert(converter = StringListToTextAttributeConverter::class)
    @Column(name = "elevator_image_urls")
    var oldElevatorImageUrls: List<String> = oldElevatorImageUrls

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
