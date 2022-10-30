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
    val targetBuildings: List<ClubQuestTargetBuilding> = dryRunResultItem.targetBuildings

    override fun equals(other: Any?): Boolean {
        return other is ClubQuest && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object
}
