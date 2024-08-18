package club.staircrusher.external_accessibility.domain.model

import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory
import club.staircrusher.stdlib.geography.Location
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
class ExternalAccessibility(
    @Id
    val id: String,
    val name: String,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "lng", column = Column(name = "location_x")),
        AttributeOverride(name = "lat", column = Column(name = "location_y")),
    )
    val location: Location,
    val address: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    @Enumerated(EnumType.STRING)
    val category: ExternalAccessibilityCategory,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "TEXT")
    val toiletDetails: ToiletAccessibilityDetails?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExternalAccessibility

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ExternalAccessibility(id='$id', name='$name', location=$location, address='$address', " +
            "createdAt=$createdAt, updatedAt=$updatedAt, category=$category, toiletDetails=$toiletDetails)"
    }
}
