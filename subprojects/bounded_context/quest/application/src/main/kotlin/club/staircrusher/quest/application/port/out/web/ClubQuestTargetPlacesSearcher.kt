package club.staircrusher.quest.application.port.out.web

import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.stdlib.geography.Location

interface ClubQuestTargetPlacesSearcher {
    suspend fun search(centerLocation: Location, radiusMeters: Int): List<ClubQuestTargetPlace>
}
