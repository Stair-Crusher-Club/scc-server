package club.staircrusher.quest.domain.vo

import club.staircrusher.stdlib.geography.Location

data class ClubQuestCreateDryRunResultItem(
    val questCenterLocation: Location,
    val targetPlaces: List<ClubQuestTargetPlace>,
)
