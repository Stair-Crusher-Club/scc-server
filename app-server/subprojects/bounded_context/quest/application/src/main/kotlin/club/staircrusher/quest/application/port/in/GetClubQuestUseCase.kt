package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class GetClubQuestUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
    private val clubQuestDtoAggregator: ClubQuestDtoAggregator,
) {
    fun handle(clubQuestId: String): ClubQuestWithDtoInfo = transactionManager.doInTransaction {
        val clubQuest = clubQuestRepository.findByIdOrNull(clubQuestId)
            ?: throw SccDomainException("Quest not found: $clubQuestId")

        clubQuestDtoAggregator.withDtoInfo(clubQuest)
    }
}
