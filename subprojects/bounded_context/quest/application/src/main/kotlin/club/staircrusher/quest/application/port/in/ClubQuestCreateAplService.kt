package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.web.AccessibilityService
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.quest.application.port.out.web.PlaceClusterer
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.stdlib.geography.Location
import kotlinx.coroutines.runBlocking
import club.staircrusher.stdlib.di.annotation.Component
import java.time.Clock

// TODO: 트랜잭션 처리
@Component
class ClubQuestCreateAplService(
    private val clock: Clock,
    private val clubQuestTargetPlacesSearcher: ClubQuestTargetPlacesSearcher,
    private val accessibilityService: AccessibilityService,
    private val clubQuestRepository: ClubQuestRepository,
    private val placeClusterer: PlaceClusterer,
) {
    fun createDryRun(
        centerLocation: Location,
        radiusMeters: Int,
        clusterCount: Int,
    ): List<ClubQuestCreateDryRunResultItem> {
        val clubQuestTargetPlaces = runBlocking {
            clubQuestTargetPlacesSearcher.search(centerLocation, radiusMeters)
        }
        val accessibilityExistingPlaceIds = accessibilityService.filterAccessibilityExistingPlaceIds(clubQuestTargetPlaces.map { it.placeId }).toSet()
        return clubQuestTargetPlaces
            .filterNot { it.placeId in accessibilityExistingPlaceIds }
            .let { placeClusterer.clusterPlaces(it, clusterCount) }
            .toList()
            .map { (questCenterLocation, belongingTargetPlaces) ->
                val targetBuildings = belongingTargetPlaces
                    .groupBy { it.buildingId }
                    .map { (buildingId, places) ->
                        ClubQuestTargetBuilding(
                            buildingId = buildingId,
                            name = buildingId, // FIXME
                            location = places.first().location, // FIXME?
                            places = places,
                        )
                    }
                ClubQuestCreateDryRunResultItem(
                    questCenterLocation = questCenterLocation,
                    targetBuildings = targetBuildings,
                )
            }
    }

    fun createFromDryRunResult(
        questNamePrefix: String,
        dryRunResultItems: List<ClubQuestCreateDryRunResultItem>
    ) {
        dryRunResultItems.forEachIndexed { idx, dryRunResultItem ->
            clubQuestRepository.save(ClubQuest(
                name = "$questNamePrefix $idx",
                dryRunResultItem = dryRunResultItem,
                createdAt = clock.instant(),
            ))
        }
    }
}
