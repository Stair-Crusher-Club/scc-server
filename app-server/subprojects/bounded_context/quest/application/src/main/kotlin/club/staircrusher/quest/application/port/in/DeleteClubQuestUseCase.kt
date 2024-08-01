package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeleteClubQuestUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
) {
    fun handle(clubQuestId: String) = transactionManager.doInTransaction {
        clubQuestRepository.deleteById(clubQuestId)
    }
}
