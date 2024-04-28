package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.geography.Location

data class ClubQuestCreateDryRunResultItem(
    val questNamePostfix: String,
    val questCenterLocation: Location,
    val targetBuildings: List<ClubQuestTargetBuildingVO>,
)
