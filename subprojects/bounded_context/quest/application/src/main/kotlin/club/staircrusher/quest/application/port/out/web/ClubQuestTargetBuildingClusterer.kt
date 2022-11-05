package club.staircrusher.quest.application.port.out.web

import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.stdlib.geography.Location

interface ClubQuestTargetBuildingClusterer {
    fun clusterBuildings(buildings: List<ClubQuestTargetBuilding>, clusterCount: Int): Map<Location, List<ClubQuestTargetBuilding>>
}
