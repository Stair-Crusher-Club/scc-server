package club.staircrusher.quest.domain.service

import club.staircrusher.quest.domain.vo.ClubQuestTargetPlace
import club.staircrusher.stdlib.geography.Location

interface ClubQuestTargetPlacesSearcher {
    suspend fun search(centerLocation: Location, radiusMeters: Int): List<ClubQuestTargetPlace>
}
