package club.staircrusher.quest.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.place.application.port.`in`.PlaceCrawler
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetBuildingClusterer
import club.staircrusher.quest.application.port.out.web.UrlShorteningService
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.model.ClubQuestPurposeType
import club.staircrusher.quest.domain.model.DryRunnedClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.DryRunnedClubQuestTargetPlace
import club.staircrusher.quest.util.HumanReadablePrefixGenerator
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.place.PlaceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Component
class ClubQuestCreateAplService(
    private val clock: Clock,
    private val placeCrawler: PlaceCrawler,
    private val clubQuestRepository: ClubQuestRepository,
    private val clubQuestTargetBuildingClusterer: ClubQuestTargetBuildingClusterer,
    private val transactionManager: TransactionManager,
    private val accessibilityApplicationService: AccessibilityApplicationService,
    private val urlShorteningService: UrlShorteningService,
    private val placeApplicationService: PlaceApplicationService,
) {
    suspend fun createDryRun(
        regionType: ClubQuestCreateRegionType?,
        centerLocation: Location?,
        radiusMeters: Int?,
        points: List<Location>?,
        clusterCount: Int,
        maxPlaceCountPerQuest: Int,
        useAlreadyCrawledPlace: Boolean,
    ): List<ClubQuestCreateDryRunResultItem> = withContext(Dispatchers.IO) {
        val places = if (useAlreadyCrawledPlace) {
            when (regionType) {
                null, ClubQuestCreateRegionType.CIRCLE -> {
                    check(centerLocation != null) { "`centerLocation` should not be null if regionType is `CIRCLE`." }
                    check(radiusMeters != null) { "`radiusMeters` should not be null if regionType is `CIRCLE`." }
                    placeApplicationService.searchPlacesInCircle(centerLocation, radiusMeters)
                }

                ClubQuestCreateRegionType.POLYGON -> {
                    check(points != null) { "`points` should not be null if regionType is `POLYGON`." }
                    check(points.size >= 3) { "최소 3개 이상의 점을 찍어야 합니다." }
                    placeApplicationService.searchPlacesInPolygon(points)
                }
            }
        } else {
            when (regionType) {
                null, ClubQuestCreateRegionType.CIRCLE -> {
                    check(centerLocation != null) { "`centerLocation` should not be null if regionType is `CIRCLE`." }
                    check(radiusMeters != null) { "`radiusMeters` should not be null if regionType is `CIRCLE`." }
                    placeCrawler.crawlPlacesInCircle(centerLocation, radiusMeters)
                }

                ClubQuestCreateRegionType.POLYGON -> {
                    check(points != null) { "`points` should not be null if regionType is `POLYGON`." }
                    check(points.size >= 3) { "최소 3개 이상의 점을 찍어야 합니다." }
                    placeCrawler.crawlPlacesInPolygon(points)
                }
            }
        }
            .filter { it.category in questTargetPlaceCategories }
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

        val questCandidates = clusteredBuildings
            .flatMap { (questCenterLocation, targetBuildings) ->
                val targets = buildingToPlaces.filter { it.key in targetBuildings }
                val chunkedBuildings = chunkByMaxPlaceCountPerQuest(targets, maxPlaceCountPerQuest)

                chunkedBuildings.map { chunk -> questCenterLocation to chunk }
            }
            .sortedByDescending { (_, targetBuildings) -> targetBuildings.values.sumOf { it.size } }

        val quests = questCandidates.take(clusterCount)
        val remainingPlaces = questCandidates
            .subList(quests.size, questCandidates.size)
            .flatMap { (_, placesByBuilding) -> placesByBuilding.values.flatten() }
            .toMutableList()
        val placeCountAdjustedQuests = quests.map { quest ->
            val placesInQuest = quest.second.values.flatten()
            if (remainingPlaces.isNotEmpty() && placesInQuest.size < maxPlaceCountPerQuest) {
                val countToSupply = minOf(maxPlaceCountPerQuest - placesInQuest.size, remainingPlaces.size)
                val placesToSupply = mutableListOf<Place>()
                repeat(countToSupply) {
                    placesToSupply += remainingPlaces.removeFirst()
                }
                Pair(quest.first, (placesInQuest + placesToSupply).groupBy { it.building })
            } else {
                quest
            }
        }

        placeCountAdjustedQuests
            .map { (location, targetBuildings) ->
                val b = targetBuildings
                    .flatMap { it.value }
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
            .chunked(maxPlaceCountPerQuest)
            .map { chunk -> chunk.groupBy({ (building, ) -> building }, { (_, place) -> place }) }
    }

    fun createFromDryRunResult(
        questNamePrefix: String,
        purposeType: ClubQuestPurposeType,
        startAt: Instant,
        endAt: Instant,
        dryRunResultItems: List<ClubQuestCreateDryRunResultItem>,
    ): List<ClubQuest> {
        val quests = transactionManager.doInTransaction {
            dryRunResultItems.mapIndexed { idx, dryRunResultItem ->
                clubQuestRepository.save(
                    ClubQuest.of(
                        name = "$questNamePrefix - ${getQuestNamePostfix(idx)}",
                        purposeType = purposeType,
                        startAt = startAt,
                        endAt = endAt,
                        dryRunResultItem = dryRunResultItem,
                        createdAt = clock.instant(),
                    )
                )
            }
        }

        return runBlocking {
            quests.map { quest ->
                async {
                    val shortenedAdminUrl = try {
                        val originalAdminUrl = quest.originalAdminUrl
                        urlShorteningService.shorten(
                            originalAdminUrl,
                            expiryDuration = Duration.ofDays(365 * 100),
                        )
                    } catch (t: Throwable) {
                        logger.error("Failed to create shortened url for ClubQuest(${quest.id})", t)
                        return@async quest
                    }
                    transactionManager.doInTransaction {
                        val reloadedQuest = clubQuestRepository.findById(quest.id).get()
                        reloadedQuest.updateShortenedAdminUrl(shortenedAdminUrl)
                        clubQuestRepository.save(reloadedQuest)
                    }
                }
            }.awaitAll()
        }
    }

    private fun getBuildingName(idx: Int): String {
        return "${idx + 1}번"
    }

    private fun getQuestNamePostfix(idx: Int): String {
        return "${HumanReadablePrefixGenerator.generateByAlphabet(idx)}조"
    }

    companion object {
        private val logger = KotlinLogging.logger {  }
        private val questTargetPlaceCategories = listOf(
            PlaceCategory.RESTAURANT,
            PlaceCategory.CAFE,
            PlaceCategory.MARKET,
            PlaceCategory.HOSPITAL,
            PlaceCategory.PHARMACY,
            PlaceCategory.CONVENIENCE_STORE
        )
    }
}
