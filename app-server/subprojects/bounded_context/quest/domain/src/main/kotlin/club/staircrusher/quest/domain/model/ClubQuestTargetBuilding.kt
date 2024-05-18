package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.geography.Location
import java.time.Instant

class ClubQuestTargetBuilding(
    val id: String,
    val clubQuestId: String,
    val buildingId: String,
    val name: String,
    val location: Location,
    val places: List<ClubQuestTargetPlace>,
    createdAt: Instant = SccClock.instant(),
    updatedAt: Instant = SccClock.instant(),
) {
    val createdAt: Instant = createdAt

    var updatedAt: Instant = updatedAt
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClubQuestTargetBuilding

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ClubQuestTargetBuilding(id='$id', clubQuestId='$clubQuestId', buildingId='$buildingId', " +
            "name='$name', location=$location, places=$places, createdAt=$createdAt, updatedAt=$updatedAt)"
    }

    companion object {
        fun of(valueObject: DryRunnedClubQuestTargetBuilding, clubQuestId: String): ClubQuestTargetBuilding {
            val id = EntityIdGenerator.generateRandom()
            return ClubQuestTargetBuilding(
                id = id,
                clubQuestId = clubQuestId,
                buildingId = valueObject.buildingId,
                name = valueObject.name,
                location = valueObject.location,
                places = valueObject.places.map {
                    ClubQuestTargetPlace.of(
                        valueObject = it,
                        clubQuestId = clubQuestId,
                        targetBuildingId = id,
                    )
                },
            )
        }
    }
}
