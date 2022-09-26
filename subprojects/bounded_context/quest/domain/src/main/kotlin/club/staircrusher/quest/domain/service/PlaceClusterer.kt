package club.staircrusher.quest.domain.service

import club.staircrusher.quest.domain.vo.ClubQuestTargetPlace
import club.staircrusher.stdlib.geography.Location

interface PlaceClusterer {
    fun clusterPlaces(places: List<ClubQuestTargetPlace>, clusterCount: Int): Map<Location, List<ClubQuestTargetPlace>>
}
