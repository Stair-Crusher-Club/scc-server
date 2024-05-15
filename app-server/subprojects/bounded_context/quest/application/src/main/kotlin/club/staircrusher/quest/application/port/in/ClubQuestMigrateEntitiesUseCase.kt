package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetBuildingRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class ClubQuestMigrateEntitiesUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
    private val clubQuestTargetBuildingRepository: ClubQuestTargetBuildingRepository,
    private val clubQuestTargetPlaceRepository: ClubQuestTargetPlaceRepository,
) {
    fun handle() {
        val allClubQuestIds = transactionManager.doInTransaction {
            clubQuestRepository.findAllOrderByCreatedAtDesc().map { it.id }
        }
        allClubQuestIds.forEach { clubQuestId ->
            transactionManager.doInTransaction {
                val clubQuest = clubQuestRepository.findById(clubQuestId)
                val targetBuildings = clubQuest.targetBuildings
                    .map { targetBuildingVO ->
                        val existingTargetBuilding = clubQuestTargetBuildingRepository.findByClubQuestIdAndBuildingId(
                            clubQuestId = clubQuest.id,
                            buildingId = targetBuildingVO.buildingId,
                        )
                        existingTargetBuilding ?: ClubQuestTargetBuilding.of(
                            valueObject = targetBuildingVO,
                            clubQuestId = clubQuest.id,
                        )
                    }
                val targetBuildingByBuildingId = targetBuildings.associateBy { it.buildingId }
                val targetPlaces = clubQuest.targetBuildings
                    .flatMap { it.places }
                    .map { targetPlaceVO ->
                        val existingTargetPlace = clubQuestTargetPlaceRepository.findByClubQuestIdAndPlaceId(
                            clubQuestId = clubQuest.id,
                            placeId = targetPlaceVO.placeId,
                        )
                        if (existingTargetPlace != null) {
                            existingTargetPlace.setIsClosed(targetPlaceVO.isClosed)
                            existingTargetPlace.setIsNotAccessible(targetPlaceVO.isNotAccessible)
                            existingTargetPlace
                        } else {
                            ClubQuestTargetPlace.of(
                                valueObject = targetPlaceVO,
                                clubQuestId = clubQuest.id,
                                targetBuildingId = targetBuildingByBuildingId[targetPlaceVO.buildingId]!!.id
                            )
                        }
                    }
                clubQuestTargetBuildingRepository.saveAll(targetBuildings)
                clubQuestTargetPlaceRepository.saveAll(targetPlaces)
            }
        }
    }
}
