package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetBuildingRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeleteClubQuestUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
    private val clubQuestTargetBuildingRepository: ClubQuestTargetBuildingRepository,
    private val clubQuestTargetPlaceRepository: ClubQuestTargetPlaceRepository,
) {
    fun handle(clubQuestId: String) = transactionManager.doInTransaction {
        clubQuestRepository.remove(clubQuestId)
        clubQuestTargetBuildingRepository.removeByClubQuestId(clubQuestId)
        clubQuestTargetPlaceRepository.removeByClubQuestId(clubQuestId)
    }
}
