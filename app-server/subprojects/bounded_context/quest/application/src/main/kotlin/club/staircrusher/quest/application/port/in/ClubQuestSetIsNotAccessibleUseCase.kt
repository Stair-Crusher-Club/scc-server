package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.quest.application.port.out.web.ConqueredPlaceService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class ClubQuestSetIsNotAccessibleUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
    private val conqueredPlaceService: ConqueredPlaceService,
    private val clubQuestTargetPlaceRepository: ClubQuestTargetPlaceRepository,
) {
    fun handle(
        clubQuestId: String,
        buildingId: String,
        placeId: String,
        isNotAccessible: Boolean,
    ): ClubQuestWithDtoInfo = transactionManager.doInTransaction {
        // Deprecated VOs should keep being written
        // until all read access on VOs is replaced to read access on entities.
        val clubQuest = clubQuestRepository.findById(clubQuestId)
        clubQuest.setIsNotAccessible(buildingId, placeId, isNotAccessible)
        clubQuestRepository.save(clubQuest)

        // dual write for read access transition period.
        val clubQuestTargetPlace = clubQuestTargetPlaceRepository.findByClubQuestIdAndPlaceId(
            clubQuestId = clubQuestId,
            placeId = placeId,
        )
        if (clubQuestTargetPlace != null) {
            clubQuestTargetPlace.setIsNotAccessible(isNotAccessible)
            clubQuestTargetPlaceRepository.save(clubQuestTargetPlace)
        }

        ClubQuestWithDtoInfo(
            quest = clubQuest,
            conqueredPlaceIds = conqueredPlaceService.getConqueredPlaceIds(clubQuest),
        )
    }
}
