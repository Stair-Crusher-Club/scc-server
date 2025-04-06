package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetBuildingRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeleteClubQuestTargetPlaceUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestTargetPlaceRepository: ClubQuestTargetPlaceRepository,
    private val clubQuestTargetBuildingRepository: ClubQuestTargetBuildingRepository,
) {
    fun handle(clubQuestId: String, placeId: String) = transactionManager.doInTransaction {
        val targetPlace = clubQuestTargetPlaceRepository.findFirstByClubQuestIdAndPlaceId(clubQuestId, placeId) ?: return@doInTransaction
        val targetBuilding = clubQuestTargetBuildingRepository.findFirstByClubQuestIdAndBuildingId(clubQuestId, targetPlace.buildingId)

        clubQuestTargetPlaceRepository.delete(targetPlace)
        targetBuilding?.removePlace(targetPlace)

        if (targetBuilding?.places?.isEmpty() == true) {
            clubQuestTargetBuildingRepository.delete(targetBuilding)
        }
    }
}
