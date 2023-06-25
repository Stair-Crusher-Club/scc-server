package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.web.ConqueredPlaceService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class GetClubQuestUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
    private val conqueredPlaceService: ConqueredPlaceService,
) {
    fun handle(clubQuestId: String): ClubQuestWithDtoInfo = transactionManager.doInTransaction {
        val clubQuest = clubQuestRepository.findById(clubQuestId)
        ClubQuestWithDtoInfo(
            quest = clubQuest,
            conqueredPlaceIds = conqueredPlaceService.getConqueredPlaceIds(clubQuest),
        )
    }
}
