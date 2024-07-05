package club.staircrusher.quest.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetBuildingClusterer
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.model.DryRunnedClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.DryRunnedClubQuestTargetPlace
import club.staircrusher.quest.util.HumanReadablePrefixGenerator
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    suspend fun createDryRun(
        regionType: ClubQuestCreateRegionType?,
        centerLocation: Location?,
        radiusMeters: Int?,
        points: List<Location>?,
        clusterCount: Int,
        maxPlaceCountPerQuest: Int,
    ): List<ClubQuestCreateDryRunResultItem> = withContext(Dispatchers.IO) {
        val places = when (regionType) {
            null, ClubQuestCreateRegionType.CIRCLE -> {
                check(centerLocation != null) { "`centerLocation` should not be null if regionType is `CIRCLE`." }
                check(radiusMeters != null) { "`radiusMeters` should not be null if regionType is `CIRCLE`." }
                clubQuestTargetPlacesSearcher.searchPlacesInCircle(centerLocation, radiusMeters)
            }
            ClubQuestCreateRegionType.POLYGON -> {
                check(points != null) { "`points` should not be null if regionType is `POLYGON`." }
                check(points.size >= 3) { "최소 3개 이상의 점을 찍어야 합니다." }
                clubQuestTargetPlacesSearcher.searchPlacesInPolygon(points)
            }
        }
        val accessibilityExistingPlaceIds = transactionManager.doInTransaction {
            accessibilityApplicationService.filterAccessibilityExistingPlaceIds(
                places.map { it.id }
            ).toSet()
        }

        val buildingToPlaces = places
            .filter { it.id !in accessibilityExistingPlaceIds && !it.isClosed && !it.isNotAccessible }
            .groupBy { it.building }
            .mapValues { (_, places) -> places.distinctBy { it.id } }
        val buildings = buildingToPlaces.keys.toList()
        val clusteredBuildings = clubQuestTargetBuildingClusterer.clusterBuildings(buildings, clusterCount)

        val quests = clusteredBuildings.flatMap { (questCenterLocation, targetBuildings) ->
            // take 2 * maxPlaceCountPerQuest places first and then cross validate them
            val targets = buildingToPlaces.filter { it.key in targetBuildings }
            val chunkedBuildings = chunkByMaxPlaceCountPerQuest(targets, maxPlaceCountPerQuest * 2)
            chunkedBuildings.map { chunk -> questCenterLocation to chunk }
        }

        quests
            .sortedByDescending { (_, targetBuildings) -> targetBuildings.values.sumOf { it.size } }
            .take(clusterCount) // clusterCount 개의 퀘스트만 만든다.
            .map { (location, targetBuildings) ->
                val b = targetBuildings
                    // 네이버 지도 api의 rate limit이 너무 낮아서 임시로 비활성화한다.
                    .flatMap { it.value }
//                    .flatMap { (_, places) ->
//                        val validationResults = clubQuestTargetPlacesSearcher.crossValidatePlaces(places)
//                        places.filterIndexed { index, _ -> validationResults[index] }
//                    }
                    .take(maxPlaceCountPerQuest)
                    .groupToClubQuestTargetBuildings()

                location to b
            }
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

    private fun List<Place>.groupToClubQuestTargetBuildings(): List<DryRunnedClubQuestTargetBuilding> {
        return this
            .groupBy { it.building.id }
            .toList().mapIndexed { buildingIdx, (buildingId, places) ->
                DryRunnedClubQuestTargetBuilding(
                    buildingId = buildingId,
                    // FIXME: 단어 목록이 부족해서 여기서 getBuildingName()을 하면 단어 목록 개수 제한으로 에러가 난다.
                    //        따라서 여기서는 임시값을 넣어주고, applyTargetPlacesCountLimitOfSingleQuest()로
                    //        퀘스트 당 건물 수를 제한한 이후에 getBuildingName()으로 건물 이름을 override 해준다.
                    name = "temp",
                    location = places.first().location,
                    places = places.map {
                        DryRunnedClubQuestTargetPlace(
                            name = it.name,
                            location = it.location,
                            placeId = it.id,
                            buildingId = it.building.id,
                        )
                    }.distinctBy { it.placeId },
                )
            }
    }

    private fun List<Pair<Location, List<DryRunnedClubQuestTargetBuilding>>>.convertToClubQuestCreateDryRunResultItems(): List<ClubQuestCreateDryRunResultItem> {
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
        targetBuildings: Map<Building, List<Place>>,
        maxPlaceCountPerQuest: Int,
    ) : List<Map<Building, List<Place>>> {
        return targetBuildings
            .flatMap { (building, places) ->
                places.map { building to it }
            }
            .shuffled()
            .chunked(maxPlaceCountPerQuest)
            .map { chunk -> chunk.groupBy({ (building, ) -> building }, { (_, place) -> place }) }
    }

    fun createFromDryRunResult(
        questNamePrefix: String,
        dryRunResultItems: List<ClubQuestCreateDryRunResultItem>
    ) = transactionManager.doInTransaction {
        dryRunResultItems.mapIndexed { idx, dryRunResultItem ->
            clubQuestRepository.save(ClubQuest.of(
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
