package club.staircrusher.quest.application.port.out.web

import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.stdlib.geography.Location

interface ClubQuestTargetPlacesSearcher {
    suspend fun searchClubQuestTargetPlaces(centerLocation: Location, radiusMeters: Int): List<ClubQuestTargetBuilding>
}
