package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
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
) : ClubQuestTargetPlacesSearcher {
    private val targetPlaceCategories = listOf(
        PlaceCategory.RESTAURANT,
        PlaceCategory.CAFE,
        PlaceCategory.MARKET,
        PlaceCategory.HOSPITAL,
        PlaceCategory.PHARMACY,
    )

    override suspend fun search(centerLocation: Location, radiusMeters: Int): List<ClubQuestTargetPlace> {
        return coroutineScope {
            targetPlaceCategories
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
                .flatMap { places ->
                    places.map {
                        ClubQuestTargetPlace(
                            name = it.name,
                            location = it.location,
                            placeId = it.id,
                            buildingId = it.building!!.id,
                            isClosed = false,
                            isNotAccessible = false,
                        )
                    }
                }
        }
    }
}
