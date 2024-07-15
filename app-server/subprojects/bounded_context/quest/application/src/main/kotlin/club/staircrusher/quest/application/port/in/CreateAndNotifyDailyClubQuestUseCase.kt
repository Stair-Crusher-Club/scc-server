package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.domain.model.ClubQuestPurposeType
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
import java.time.Duration

@Component
class CreateAndNotifyDailyClubQuestUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestCreateAplService: ClubQuestCreateAplService,
    private val mapsService: MapsService,
) {
    data class Result(
        val clubQuest: ClubQuest,
        val url: String,
    )

    fun handle(
        requesterName: String,
        @Suppress("UnusedPrivateMember") requesterPhoneNumber: String,
        centerLocationPlaceName: String,
        maxPlaceCountPerQuest: Int,
    ): Result {
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
            val now = SccClock.instant()
            clubQuestCreateAplService.createFromDryRunResult(
                "${requesterName}님을 위한 일상 퀘스트",
                purposeType = ClubQuestPurposeType.DAILY_CLUB,
                startAt = now,
                endAt = now + CLUB_QUEST_EXPIRY_DURATION,
                dryRunResultItems = dryRunResultItems,
            )[0]
        }.let {
            Result(
                clubQuest = it,
                url = it.shortenedAdminUrl ?: it.originalAdminUrl
            )
        }
    }

    companion object {
        private const val CLUB_QUEST_REGION_RADIUS_METERS = 300
        private val CLUB_QUEST_EXPIRY_DURATION = Duration.ofDays(14)!!
    }
}
