package club.staircrusher.accessibility.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.jpa.LocationListToTextAttributeConverter
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class AccessibilityAllowedRegion(
    @Id
    val id: String,
    val name: String,
    @Convert(converter = LocationListToTextAttributeConverter::class)
    val boundaryVertices: List<Location>,
    val createdAt: Instant = SccClock.instant(),
    updatedAt: Instant = createdAt,
) {
    constructor(
        name: String,
        boundaryVertices: List<Location>,
    ) : this(
        id = EntityIdGenerator.generateRandom(),
        name = name,
        boundaryVertices = boundaryVertices,
        createdAt = SccClock.instant(),
    )

    var updatedAt: Instant = updatedAt
        private set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccessibilityAllowedRegion

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "AccessibilityAllowedRegion(id='$id', name='$name', boundaryVertices=$boundaryVertices, " +
            "createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}
