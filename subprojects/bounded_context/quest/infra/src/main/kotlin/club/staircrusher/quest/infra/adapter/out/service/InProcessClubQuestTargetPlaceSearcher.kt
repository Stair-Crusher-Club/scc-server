package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.quest.application.port.out.web.AccessibilityService
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import club.staircrusher.stdlib.di.annotation.Component

@Component
class InProcessClubQuestTargetPlaceSearcher(
    private val placeService: PlaceService,
    private val accessibilityService: AccessibilityService,
) : ClubQuestTargetPlacesSearcher {
    private val targetPlaceCategories = listOf(
        PlaceCategory.RESTAURANT,
        PlaceCategory.CAFE,
        PlaceCategory.MARKET,
        PlaceCategory.HOSPITAL,
        PlaceCategory.PHARMACY,
    )

    override suspend fun searchClubQuestTargetPlaces(centerLocation: Location, radiusMeters: Int): List<ClubQuestTargetBuilding> {
        return coroutineScope {
            val places = targetPlaceCategories
                .map {
                    async {
                        placeService.findAllByCategory(
                            it, MapsService.SearchOption(
                                region = MapsService.SearchOption.CircleRegion(
                                    centerLocation = centerLocation,
                                    radiusMeters = radiusMeters,
                                ),
                            )
                        )
                    }
                }
                .let { awaitAll(*it.toTypedArray()) }
                .flatten()
            val accessibilityExistingPlaceIds = accessibilityService.filterAccessibilityExistingPlaceIds(
                places.map { it.id }
            ).toSet()
            places
                .filter { it.id !in accessibilityExistingPlaceIds }
                .groupBy { it.building!!.id }
                .map { (buildingId, places) ->
                    ClubQuestTargetBuilding(
                        buildingId = buildingId,
                        name = places.first().address.toString(),
                        location = places.first().location,
                        places = places.map {
                            ClubQuestTargetPlace(
                                name = it.name,
                                location = it.location,
                                placeId = it.id,
                                buildingId = it.building!!.id,
                                isClosed = false,
                                isNotAccessible = false,
                            )
                        },
                    )
                }
        }
    }
}
