package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.geography.Location
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.time.Instant

@Entity
class ClubQuestTargetBuilding(
    @Id
    val id: String,
    val clubQuestId: String,
    val buildingId: String,
    val name: String,
    @AttributeOverrides(
        AttributeOverride(name = "lng", column = Column(name = "location_x")),
        AttributeOverride(name = "lat", column = Column(name = "location_y")),
    )
    val location: Location,
    @OneToMany(mappedBy = "targetBuildingId", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val places: MutableList<ClubQuestTargetPlace>,
    val createdAt: Instant = SccClock.instant(),
    private val updatedAt: Instant = SccClock.instant(),
) {
    fun removePlace(targetPlace: ClubQuestTargetPlace) {
        places.remove(targetPlace)
    }

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
                }.toMutableList(),
            )
        }
    }
}
