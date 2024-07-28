package club.staircrusher.quest.application.port.out.persistence

import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import org.springframework.data.repository.CrudRepository

interface ClubQuestTargetPlaceRepository : CrudRepository<ClubQuestTargetPlace, String> {
    fun findFirstByClubQuestIdAndPlaceId(clubQuestId: String, placeId: String): ClubQuestTargetPlace?
    fun findByClubQuestIdAndPlaceIdIn(clubQuestId: String, placeIds: List<String>): List<ClubQuestTargetPlace>
}
