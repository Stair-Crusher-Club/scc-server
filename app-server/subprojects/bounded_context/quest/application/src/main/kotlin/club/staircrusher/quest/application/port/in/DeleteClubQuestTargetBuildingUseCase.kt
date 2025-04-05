package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetBuildingRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeleteClubQuestTargetBuildingUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestTargetPlaceRepository: ClubQuestTargetPlaceRepository,
    private val clubQuestTargetBuildingRepository: ClubQuestTargetBuildingRepository,
) {
    fun handle(clubQuestId: String, buildingId: String) = transactionManager.doInTransaction {
        val targetBuilding = clubQuestTargetBuildingRepository.findFirstByClubQuestIdAndBuildingId(clubQuestId, buildingId) ?: return@doInTransaction
        clubQuestTargetPlaceRepository.deleteAll(targetBuilding.places)
        clubQuestTargetBuildingRepository.delete(targetBuilding)
    }
}
