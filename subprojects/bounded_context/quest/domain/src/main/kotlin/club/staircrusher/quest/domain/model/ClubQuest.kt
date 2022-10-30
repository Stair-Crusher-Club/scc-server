package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.geography.Location
import java.time.Instant

class ClubQuest(
    val id: String,
    val name: String,
    dryRunResultItem: ClubQuestCreateDryRunResultItem,
    val createdAt: Instant,
) {
    val questCenterLocation: Location = dryRunResultItem.questCenterLocation
    var targetBuildings: List<ClubQuestTargetBuilding> = dryRunResultItem.targetBuildings
        internal set

    fun setIsClosed(buildingId: String, placeId: String, value: Boolean) {
        targetBuildings = targetBuildings.replaced({ it.buildingId == buildingId }) { building ->
            building.copy(
                places = building.places.replaced({ it.placeId == placeId }) { place ->
                    place.copy(isClosed = value)
                },
            )
        }
    }
    fun setIsNotAccessible(buildingId: String, placeId: String, value: Boolean) {
        targetBuildings = targetBuildings.replaced({ it.buildingId == buildingId }) { building ->
            building.copy(
                places = building.places.replaced({ it.placeId == placeId }) { place ->
                    place.copy(isNotAccessible = value)
                },
            )
        }
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
