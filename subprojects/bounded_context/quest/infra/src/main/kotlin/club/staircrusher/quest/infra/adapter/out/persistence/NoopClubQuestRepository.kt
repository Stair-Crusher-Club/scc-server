package club.staircrusher.quest.infra.adapter.out.persistence

import club.staircrusher.quest.domain.entity.ClubQuest
import club.staircrusher.quest.domain.repository.ClubQuestRepository
import club.staircrusher.stdlib.di.annotation.Component

@Component
class NoopClubQuestRepository : ClubQuestRepository {
    override fun save(clubQuest: ClubQuest) {
        // no-op
    }
}
