package club.staircrusher.quest.application.port.out.persistence

import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ClubQuestTargetBuildingRepository : EntityRepository<ClubQuestTargetBuilding, String> {
    fun removeByClubQuestId(clubQuestId: String)
    fun findByClubQuestIdAndBuildingId(clubQuestId: String, buildingId: String): ClubQuestTargetBuilding?
}
