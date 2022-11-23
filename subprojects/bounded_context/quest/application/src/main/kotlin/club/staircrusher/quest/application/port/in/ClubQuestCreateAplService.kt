package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.domain.model.Place
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.web.AccessibilityService
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetBuildingClusterer
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.quest.util.HumanReadablePrefixGenerator
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
        val invalidPlaceIds = transactionManager.doInTransaction {
            getInvalidPlaceIds()
        }
        return places
            .filter { it.id !in accessibilityExistingPlaceIds && it.id !in invalidPlaceIds }
            .groupToClubQuestTargetBuildings()
            .let { clubQuestTargetBuildingClusterer.clusterBuildings(it, clusterCount) }
            .convertToClubQuestCreateDryRunResultItems()
    }

    private fun getInvalidPlaceIds(): Set<String> {
        return clubQuestRepository.findAllOrderByCreatedAtDesc()
            .flatMap { it.targetBuildings }
            .flatMap { it.places }
            .filter { it.isClosed || it.isNotAccessible }
            .map { it.placeId }
            .toSet()
    }

    private fun List<Place>.groupToClubQuestTargetBuildings(): List<ClubQuestTargetBuilding> {
        return this
            .groupBy { it.building!!.id }
            .toList().mapIndexed { buildingIdx, (buildingId, places) ->
                ClubQuestTargetBuilding(
                    buildingId = buildingId,
                    name = getBuildingName(buildingIdx),
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
                    }.distinctBy { it.placeId },
                )
            }
    }

    private fun Map<Location, List<ClubQuestTargetBuilding>>.convertToClubQuestCreateDryRunResultItems(): List<ClubQuestCreateDryRunResultItem> {
        return this
            .toList().map { (questCenterLocation, targetBuildings) ->
                Pair(questCenterLocation, applyTargetPlacesCountLimitOfSingleQuest(targetBuildings))
            }
            .mapIndexed { idx, (questCenterLocation, targetBuildings) ->
                ClubQuestCreateDryRunResultItem(
                    questNamePostfix = getQuestNamePostfix(idx),
                    questCenterLocation = questCenterLocation,
                    targetBuildings = targetBuildings,
                )
            }
    }

    @Suppress("MagicNumber") private val targetPlacesCountLimitOfSingleQuest = 50
    private fun applyTargetPlacesCountLimitOfSingleQuest(targetBuildings: List<ClubQuestTargetBuilding>): List<ClubQuestTargetBuilding> {
        val totalTargetPlaces = targetBuildings.sumOf { it.places.count() }
        if (totalTargetPlaces <= targetPlacesCountLimitOfSingleQuest) {
            return targetBuildings
        }
        return targetBuildings
            .flatMap { targetBuilding -> targetBuilding.places.map { Pair(targetBuilding, it) } }
            .shuffled()
            .take(targetPlacesCountLimitOfSingleQuest)
            .groupBy { it.first }
            .map { (targetBuilding, pairs) ->
                targetBuilding.copy(places = pairs.map { it.second })
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

    private fun getBuildingName(idx: Int): String {
        return "${HumanReadablePrefixGenerator.generate(idx)} 건물"
    }

    private fun getQuestNamePostfix(idx: Int): String {
        return "${HumanReadablePrefixGenerator.generate(idx)}조"
    }
}
