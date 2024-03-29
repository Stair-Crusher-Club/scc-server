package club.staircrusher.quest.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetBuildingClusterer
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.quest.util.HumanReadablePrefixGenerator
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
import java.time.Clock

@Component
class ClubQuestCreateAplService(
    private val clock: Clock,
    private val clubQuestTargetPlacesSearcher: ClubQuestTargetPlacesSearcher,
    private val clubQuestRepository: ClubQuestRepository,
    private val clubQuestTargetBuildingClusterer: ClubQuestTargetBuildingClusterer,
    private val transactionManager: TransactionManager,
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    fun createDryRun(
        centerLocation: Location,
        radiusMeters: Int,
        clusterCount: Int,
        maxPlaceCountPerQuest: Int,
    ): List<ClubQuestCreateDryRunResultItem> {
        val places = runBlocking {
            clubQuestTargetPlacesSearcher.searchPlaces(centerLocation, radiusMeters)
        }
        val accessibilityExistingPlaceIds = transactionManager.doInTransaction {
            accessibilityApplicationService.filterAccessibilityExistingPlaceIds(
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
            .toList()
            .flatMap { (questCenterLocation, targetBuildings) ->
                chunkByMaxPlaceCountPerQuest(targetBuildings, maxPlaceCountPerQuest)
                    .map { Pair(questCenterLocation, it) }
            }
            .sortedByDescending { (_, targetBuildings) -> targetBuildings.sumOf { it.places.size } }.take(clusterCount) // clusterCount 개의 퀘스트만 만든다.
            .map { (location, targetBuildings) ->
                Pair(
                    location,
                    targetBuildings.mapIndexed { idx, targetBuilding ->
                        targetBuilding.copy(name = getBuildingName(idx))
                    },
                )
            }
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
            .groupBy { it.building.id }
            .toList().mapIndexed { buildingIdx, (buildingId, places) ->
                ClubQuestTargetBuilding(
                    buildingId = buildingId,
                    // FIXME: 단어 목록이 부족해서 여기서 getBuildingName()을 하면 단어 목록 개수 제한으로 에러가 난다.
                    //        따라서 여기서는 임시값을 넣어주고, applyTargetPlacesCountLimitOfSingleQuest()로
                    //        퀘스트 당 건물 수를 제한한 이후에 getBuildingName()으로 건물 이름을 override 해준다.
                    name = "temp",
                    location = places.first().location,
                    places = places.map {
                        ClubQuestTargetPlace(
                            name = it.name,
                            location = it.location,
                            placeId = it.id,
                            buildingId = it.building.id,
                            isClosed = false,
                            isNotAccessible = false,
                        )
                    }.distinctBy { it.placeId },
                )
            }
    }

    private fun List<Pair<Location, List<ClubQuestTargetBuilding>>>.convertToClubQuestCreateDryRunResultItems(): List<ClubQuestCreateDryRunResultItem> {
        return this
            .mapIndexed { idx, (questCenterLocation, targetBuildings) ->
                ClubQuestCreateDryRunResultItem(
                    questNamePostfix = getQuestNamePostfix(idx),
                    questCenterLocation = questCenterLocation,
                    targetBuildings = targetBuildings,
                )
            }
    }

    /**
     * 한 클러스터 안의 장소들을 maxPlaceCountPerQuest개 만큼 자른다.
     */
    private fun chunkByMaxPlaceCountPerQuest(
        targetBuildings: List<ClubQuestTargetBuilding>,
        maxPlaceCountPerQuest: Int,
    ) : List<List<ClubQuestTargetBuilding>> {
        return targetBuildings
            .flatMap { targetBuilding -> targetBuilding.places.map { Pair(targetBuilding, it) } }
            .shuffled()
            .chunked(maxPlaceCountPerQuest)
            .map { chunk ->
                chunk
                    .groupBy({ it.first }, { it.second })
                    .map { (targetBuilding, targetPlaces) ->
                        targetBuilding.copy(places = targetPlaces)
                    }
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
        return "${idx + 1}번"
    }

    private fun getQuestNamePostfix(idx: Int): String {
        return "${HumanReadablePrefixGenerator.generateByAlphabet(idx)}조"
    }
}
