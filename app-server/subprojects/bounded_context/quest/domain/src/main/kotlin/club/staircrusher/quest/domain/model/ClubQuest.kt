package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.geography.Location
import java.time.Instant

class ClubQuest(
    val id: String,
    val name: String,
    val questCenterLocation: Location,
    targetBuildings: List<ClubQuestTargetBuildingVO>,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    constructor(
        name: String,
        dryRunResultItem: ClubQuestCreateDryRunResultItem,
        createdAt: Instant,
    ) : this(
        id = EntityIdGenerator.generateRandom(),
        name = name,
        questCenterLocation = dryRunResultItem.questCenterLocation,
        targetBuildings = dryRunResultItem.targetBuildings,
        createdAt = createdAt,
        updatedAt = createdAt,
    )

    var targetBuildings: List<ClubQuestTargetBuildingVO> = targetBuildings
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun setIsClosed(buildingId: String, placeId: String, value: Boolean) {
        targetBuildings = targetBuildings.replaced({ it.buildingId == buildingId }) { building ->
            building.copy(
                places = building.places.replaced({ it.placeId == placeId }) { place ->
                    place.copy(isClosed = value)
                },
            )
        }
        updatedAt = SccClock.instant()
    }
    fun setIsNotAccessible(buildingId: String, placeId: String, value: Boolean) {
        targetBuildings = targetBuildings.replaced({ it.buildingId == buildingId }) { building ->
            building.copy(
                places = building.places.replaced({ it.placeId == placeId }) { place ->
                    place.copy(isNotAccessible = value)
                },
            )
        }
        updatedAt = SccClock.instant()
    }

    override fun equals(other: Any?): Boolean {
        return other is ClubQuest && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    private fun <T> List<T>.replaced(predicateBlock: (T) -> Boolean, convertBlock: (T) -> T): List<T> {
        val mutableList = this.toMutableList()
        val matchingItemIdx = indexOfFirst(predicateBlock)
        val newItem = convertBlock(this[matchingItemIdx])
        mutableList.removeAt(matchingItemIdx)
        mutableList.add(matchingItemIdx, newItem)
        return mutableList.toList()
    }
}
