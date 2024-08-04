package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.geography.Location
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class ClubQuestTargetPlace(
    @Id
    val id: String,
    val clubQuestId: String,
    val targetBuildingId: String,
    val buildingId: String,
    val placeId: String,
    val name: String,
    @AttributeOverrides(
        AttributeOverride(name = "lng", column = Column(name = "location_x")),
        AttributeOverride(name = "lat", column = Column(name = "location_y")),
    )
    val location: Location,
    isClosedExpected: Boolean,
    val createdAt: Instant = SccClock.instant(),
    updatedAt: Instant = SccClock.instant(),
) {

    var updatedAt: Instant = updatedAt
        protected set

    var isClosedExpected: Boolean = isClosedExpected
        protected set

    fun expectToBeClosed() {
        isClosedExpected = true
        updatedAt = SccClock.instant()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClubQuestTargetPlace

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ClubQuestTargetPlace(id='$id', clubQuestId='$clubQuestId', buildingId='$buildingId', " +
            "placeId='$placeId', name='$name', location=$location, createdAt=$createdAt, updatedAt=$updatedAt)"
    }

    companion object {
        fun of(
            valueObject: DryRunnedClubQuestTargetPlace,
            clubQuestId: String,
            targetBuildingId: String,
        ): ClubQuestTargetPlace {
            return ClubQuestTargetPlace(
                id = EntityIdGenerator.generateRandom(),
                clubQuestId = clubQuestId,
                targetBuildingId = targetBuildingId,
                buildingId = valueObject.buildingId,
                placeId = valueObject.placeId,
                name = valueObject.name,
                location = valueObject.location,
                isClosedExpected = false,
            )
        }
    }
}
