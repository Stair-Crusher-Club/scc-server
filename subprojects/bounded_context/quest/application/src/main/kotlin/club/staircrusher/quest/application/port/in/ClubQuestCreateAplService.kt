package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetBuildingClusterer
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.stdlib.geography.Location
import kotlinx.coroutines.runBlocking
import club.staircrusher.stdlib.di.annotation.Component
import java.time.Clock

// TODO: 트랜잭션 처리
@Component
class ClubQuestCreateAplService(
    private val clock: Clock,
    private val clubQuestTargetPlacesSearcher: ClubQuestTargetPlacesSearcher,
    private val clubQuestRepository: ClubQuestRepository,
    private val clubQuestTargetBuildingClusterer: ClubQuestTargetBuildingClusterer,
) {
    fun createDryRun(
        centerLocation: Location,
        radiusMeters: Int,
        clusterCount: Int,
    ): List<ClubQuestCreateDryRunResultItem> {
        val clubQuestTargetBuildings = runBlocking {
            clubQuestTargetPlacesSearcher.searchClubQuestTargetPlaces(centerLocation, radiusMeters)
        }
        return clubQuestTargetBuildings
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
    ) {
        dryRunResultItems.forEachIndexed { idx, dryRunResultItem ->
            clubQuestRepository.save(ClubQuest(
                name = "$questNamePrefix - ${getQuestNamePostfix(idx)}",
                dryRunResultItem = dryRunResultItem,
                createdAt = clock.instant(),
            ))
        }
    }

    private fun getQuestNamePostfix(idx: Int): String {
        check(idx <= 25) { "최대 26개 지역으로만 분할 가능합니다." }
        return "${"ABCDEFGHIJKLMNOPQRSTUVWXYZ"[idx]}조"
    }
}
