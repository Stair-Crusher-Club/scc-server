package club.staircrusher.quest.application.port.out.web

import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.geography.Location

interface ClubQuestTargetPlacesSearcher {
    suspend fun searchPlaces(centerLocation: Location, radiusMeters: Int): List<Place>
}
