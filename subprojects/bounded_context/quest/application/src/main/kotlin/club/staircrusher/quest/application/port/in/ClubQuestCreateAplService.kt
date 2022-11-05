package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.web.AccessibilityService
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetBuildingClusterer
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.stdlib.geography.Location
import kotlinx.coroutines.runBlocking
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import java.time.Clock

@Component
class ClubQuestCreateAplService(
    private val clock: Clock,
    private val clubQuestTargetPlacesSearcher: ClubQuestTargetPlacesSearcher,
    private val clubQuestRepository: ClubQuestRepository,
    private val clubQuestTargetBuildingClusterer: ClubQuestTargetBuildingClusterer,
    private val transactionManager: TransactionManager,
    private val accessibilityService: AccessibilityService,
) {
    fun createDryRun(
        centerLocation: Location,
        radiusMeters: Int,
        clusterCount: Int,
    ): List<ClubQuestCreateDryRunResultItem> {
        val places = runBlocking {
            clubQuestTargetPlacesSearcher.searchPlaces(centerLocation, radiusMeters)
        }
        val accessibilityExistingPlaceIds = transactionManager.doInTransaction {
            accessibilityService.filterAccessibilityExistingPlaceIds(
                places.map { it.id }
            ).toSet()
        }
        return places
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
            .let { clubQuestTargetBuildingClusterer.clusterBuildings(it, clusterCount) }
            .toList().mapIndexed { idx, (questCenterLocation, targetBuildings) ->
                ClubQuestCreateDryRunResultItem(
                    questNamePostfix = getQuestNamePostfix(idx),
                    questCenterLocation = questCenterLocation,
                    targetBuildings = targetBuildings,
                )
            }
    }

    fun createFromDryRunResult(
        questNamePrefix: String,
        dryRunResultItems: List<ClubQuestCreateDryRunResultItem>
    ) = transactionManager.doInTransaction {
        dryRunResultItems.forEachIndexed { idx, dryRunResultItem ->
            clubQuestRepository.save(ClubQuest(
                name = "$questNamePrefix - ${getQuestNamePostfix(idx)}",
                dryRunResultItem = dryRunResultItem,
                createdAt = clock.instant(),
            ))
        }
    }

    private fun getQuestNamePostfix(idx: Int): String {
        @Suppress("MagicNumber") check(idx <= 25) { "최대 26개 지역으로만 분할 가능합니다." }
        return "${"ABCDEFGHIJKLMNOPQRSTUVWXYZ"[idx]}조"
    }
}
