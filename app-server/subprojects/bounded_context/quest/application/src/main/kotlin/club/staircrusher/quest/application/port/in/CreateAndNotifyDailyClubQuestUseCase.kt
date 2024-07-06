package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking

@Component
class CreateAndNotifyDailyClubQuestUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestCreateAplService: ClubQuestCreateAplService,
    private val mapsService: MapsService,
) {
    fun handle(
        requesterName: String,
        @Suppress("UnusedPrivateMember") requesterPhoneNumber: String,
        centerLocationPlaceName: String,
        maxPlaceCountPerQuest: Int,
    ): ClubQuest {
        val dryRunResultItems = runBlocking {
            val centerLocation = mapsService.findAllByKeyword(
                keyword = centerLocationPlaceName,
                option = MapsService.SearchByKeywordOption(),
            ).firstOrNull()?.location
                ?: throw IllegalArgumentException("No such place: $centerLocationPlaceName")

            clubQuestCreateAplService.createDryRun(
                regionType = ClubQuestCreateRegionType.CIRCLE,
                centerLocation = centerLocation,
                radiusMeters = CLUB_QUEST_REGION_RADIUS_METERS,
                points = null,
                clusterCount = 1,
                maxPlaceCountPerQuest = maxPlaceCountPerQuest,
            )
                .also { check(it.size == 1) }
        }

        return transactionManager.doInTransaction {
            clubQuestCreateAplService.createFromDryRunResult(
                "${requesterName}님을 위한 일상 퀘스트",
                dryRunResultItems = dryRunResultItems,
            )[0]
        }
        // TODO: 문자 보내기
    }

    companion object {
        private const val CLUB_QUEST_REGION_RADIUS_METERS = 300
    }
}
