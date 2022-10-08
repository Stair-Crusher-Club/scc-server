package club.staircrusher.quest.application

import club.staircrusher.quest.domain.entity.ClubQuest
import club.staircrusher.quest.domain.repository.ClubQuestRepository
import club.staircrusher.quest.domain.service.AccessibilityService
import club.staircrusher.quest.domain.service.ClubQuestTargetPlacesSearcher
import club.staircrusher.quest.domain.service.PlaceClusterer
import club.staircrusher.quest.domain.vo.ClubQuestCreateDryRunResultItem
import club.staircrusher.stdlib.geography.Location
import kotlinx.coroutines.runBlocking
import club.staircrusher.stdlib.di.annotation.Component
import java.util.UUID

// TODO: 트랜잭션 처리
@Component
class ClubQuestCreateAplService(
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
                ClubQuestCreateDryRunResultItem(
                    questCenterLocation = questCenterLocation,
                    targetPlaces = belongingTargetPlaces,
                )
            }
    }

    fun createFromDryRunResult(
        questNamePrefix: String,
        dryRunResultItems: List<ClubQuestCreateDryRunResultItem>
    ) {
        dryRunResultItems.forEachIndexed { idx, dryRunResultItem ->
            clubQuestRepository.save(ClubQuest(
                id = UUID.randomUUID().toString(), // TODO: entity id 생성 전략 수립
                name = "$questNamePrefix $idx",
                dryRunResultItem = dryRunResultItem,
            ))
        }
    }
}
