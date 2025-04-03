package club.staircrusher.quest.application.port.out.web

import club.staircrusher.place.domain.model.place.Building
import club.staircrusher.stdlib.geography.Location

interface ClubQuestTargetBuildingClusterer {
    fun clusterBuildings(buildings: List<Building>, clusterCount: Int): Map<Location, List<Building>>
}
