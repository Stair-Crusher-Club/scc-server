package club.staircrusher.quest.application.port.out.web

import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.stdlib.geography.Location

interface PlaceClusterer {
    fun clusterPlaces(places: List<ClubQuestTargetPlace>, clusterCount: Int): Map<Location, List<ClubQuestTargetPlace>>
}
