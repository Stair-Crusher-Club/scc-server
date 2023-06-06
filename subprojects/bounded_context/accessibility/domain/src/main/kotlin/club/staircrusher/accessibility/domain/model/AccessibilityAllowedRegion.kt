package club.staircrusher.accessibility.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.geography.Location
import java.time.Instant

class AccessibilityAllowedRegion(
    val id: String,
    val name: String,
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
        return other is AccessibilityAllowedRegion && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
