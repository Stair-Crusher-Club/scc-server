package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.geography.Location
import java.time.Instant

class ClubQuest(
    val id: String,
    val name: String,
    val questCenterLocation: Location,
    targetBuildings: List<ClubQuestTargetBuilding>,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    var targetBuildings: List<ClubQuestTargetBuilding> = targetBuildings
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun setIsClosed(buildingId: String, placeId: String, value: Boolean): ClubQuestTargetPlace? {
        val targetPlace = targetBuildings.flatMap { it.places }.find { it.buildingId == buildingId && it.placeId == placeId }
            ?: return null
        targetPlace.setIsClosed(value)
        return targetPlace
    }
    fun setIsNotAccessible(buildingId: String, placeId: String, value: Boolean): ClubQuestTargetPlace? {
        val targetPlace = targetBuildings.flatMap { it.places }.find { it.buildingId == buildingId && it.placeId == placeId }
            ?: return null
        targetPlace.setIsNotAccessible(value)
        return targetPlace
    }

    override fun equals(other: Any?): Boolean {
        return other is ClubQuest && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        fun of(
            name: String,
            dryRunResultItem: ClubQuestCreateDryRunResultItem,
            createdAt: Instant,
        ): ClubQuest {
            val id = EntityIdGenerator.generateRandom()
            return ClubQuest(
                id = id,
                name = name,
                questCenterLocation = dryRunResultItem.questCenterLocation,
                targetBuildings = dryRunResultItem.targetBuildings.map {
                    ClubQuestTargetBuilding.of(valueObject = it, clubQuestId = id)
                },
                createdAt = createdAt,
                updatedAt = createdAt,
            )
        }
    }
}
