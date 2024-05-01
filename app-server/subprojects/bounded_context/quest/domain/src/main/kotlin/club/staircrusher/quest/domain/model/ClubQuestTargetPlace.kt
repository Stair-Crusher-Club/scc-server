package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.geography.Location
import java.time.Instant

class ClubQuestTargetPlace(
    val id: String,
    val clubQuestId: String,
    val targetBuildingId: String,
    val buildingId: String,
    val placeId: String,
    val name: String,
    val location: Location,
    isClosed: Boolean,
    isNotAccessible: Boolean,
    createdAt: Instant = SccClock.instant(),
    updatedAt: Instant = SccClock.instant(),
) {
    var isClosed: Boolean = isClosed
        protected set

    var isNotAccessible: Boolean = isNotAccessible
        protected set

    val createdAt: Instant = createdAt

    var updatedAt: Instant = updatedAt
        protected set

    fun setIsClosed(value: Boolean) {
        isClosed = value
        updatedAt = SccClock.instant()
    }

    fun setIsNotAccessible(value: Boolean) {
        isNotAccessible = value
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
            "placeId='$placeId', name='$name', location=$location, isClosed=$isClosed, " +
            "isNotAccessible=$isNotAccessible, createdAt=$createdAt, updatedAt=$updatedAt)"
    }

    companion object {
        fun of(
            valueObject: ClubQuestTargetPlaceVO,
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
                isClosed = valueObject.isClosed,
                isNotAccessible = valueObject.isNotAccessible,
            )
        }
    }
}
