package club.staircrusher.quest.application.port.out.persistence

import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ClubQuestTargetPlaceRepository : EntityRepository<ClubQuestTargetPlace, String> {
    fun findByClubQuestIdAndPlaceId(clubQuestId: String, placeId: String): ClubQuestTargetPlace?
    fun findByClubQuestIdAndPlaceIds(clubQuestId: String, placeIds: List<String>): List<ClubQuestTargetPlace>
    fun findByTargetBuildingId(targetBuildingId: String): List<ClubQuestTargetPlace>
    fun removeByClubQuestId(clubQuestId: String)
}
