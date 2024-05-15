package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetBuildingRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
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
                    .map { ClubQuestTargetBuilding.of(valueObject = it, clubQuestId = clubQuest.id) }
                    .map { newTargetBuilding ->
                        val existingTargetBuilding = clubQuestTargetBuildingRepository.findByClubQuestIdAndBuildingId(
                            clubQuestId = clubQuest.id,
                            buildingId = newTargetBuilding.buildingId,
                        )
                        existingTargetBuilding ?: newTargetBuilding
                    }
                val targetPlaces = targetBuildings
                    .flatMap { it.places }
                    .map { newTargetPlace ->
                        val existingTargetPlace = clubQuestTargetPlaceRepository.findByClubQuestIdAndPlaceId(
                            clubQuestId = clubQuest.id,
                            placeId = newTargetPlace.placeId,
                        )
                        if (existingTargetPlace != null) {
                            existingTargetPlace.setIsClosed(newTargetPlace.isClosed)
                            existingTargetPlace.setIsNotAccessible(newTargetPlace.isNotAccessible)
                            existingTargetPlace
                        } else {
                            newTargetPlace
                        }
                    }
                clubQuestTargetBuildingRepository.saveAll(targetBuildings)
                clubQuestTargetPlaceRepository.saveAll(targetPlaces)
            }
        }
    }
}
