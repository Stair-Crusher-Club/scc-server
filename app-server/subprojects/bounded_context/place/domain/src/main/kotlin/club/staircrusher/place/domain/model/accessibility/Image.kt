package club.staircrusher.place.domain.model.accessibility

import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "accessibility_image")
class Image(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    val accessibilityId: String,

    @Column(nullable = false)
    val accessibilityType: String,

    @Column(nullable = false)
    val imageUrl: String,

    @Column(nullable = true)
    val blurredImageUrl: String? = null,

    @Column(nullable = true)
    var thumbnailUrl: String? = null,

    @Column(nullable = true)
    val imageType: String? = null, // Can be used to distinguish between entrance/elevator images for BuildingAccessibility
) : TimeAuditingBaseEntity() {

    companion object {
        const val TYPE_PLACE = "PLACE"
        const val TYPE_BUILDING_ENTRANCE = "BUILDING_ENTRANCE"
        const val TYPE_BUILDING_ELEVATOR = "BUILDING_ELEVATOR"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Image(id='$id', imageUrl='$imageUrl', thumbnailUrl=$thumbnailUrl, " +
                "accessibilityId=$accessibilityId, " +
                "imageType=$imageType)"
    }
}
