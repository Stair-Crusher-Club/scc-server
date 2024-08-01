package club.staircrusher.quest.application.port.out.persistence

import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import org.springframework.data.repository.CrudRepository

interface ClubQuestTargetBuildingRepository : CrudRepository<ClubQuestTargetBuilding, String> {
    fun findFirstByClubQuestIdAndBuildingId(clubQuestId: String, buildingId: String): ClubQuestTargetBuilding?
}
