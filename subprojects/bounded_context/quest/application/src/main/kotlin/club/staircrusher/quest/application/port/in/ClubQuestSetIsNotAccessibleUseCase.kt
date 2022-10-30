package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class ClubQuestSetIsNotAccessibleUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
) {
    fun handle(
        clubQuestId: String,
        buildingId: String,
        placeId: String,
        isClosed: Boolean,
    ): ClubQuest = transactionManager.doInTransaction {
        val clubQuest = clubQuestRepository.findById(clubQuestId)
        clubQuest.setIsNotAccessible(buildingId, placeId, isClosed)
        clubQuestRepository.save(clubQuest)
    }
}
