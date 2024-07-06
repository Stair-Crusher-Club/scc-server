package club.staircrusher.quest.application.port.out.web

import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.geography.Location

interface ClubQuestTargetPlacesSearcher {
    suspend fun searchPlacesInCircle(centerLocation: Location, radiusMeters: Int): List<Place>
    suspend fun searchPlacesInPolygon(points: List<Location>): List<Place>
    suspend fun crossValidatePlaces(places: List<Place>): List<Boolean>
}
