package club.staircrusher.quest.infra.adapter.out.persistence

import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.stdlib.di.annotation.Component

@Component
class InMemoryClubQuestRepository : ClubQuestRepository {
    private val clubQuestById = mutableMapOf<String, ClubQuest>()

    override fun save(entity: ClubQuest): ClubQuest {
        clubQuestById[entity.id] = entity
        return entity
    }

    override fun saveAll(entities: Collection<ClubQuest>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        clubQuestById.clear()
    }

    override fun findById(id: String): ClubQuest {
        return clubQuestById[id] ?: throw IllegalArgumentException("User of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): ClubQuest? {
        return clubQuestById[id]
    }

    override fun findAllOrderByCreatedAtDesc(): List<ClubQuest> {
        return clubQuestById.values.toList().sortedByDescending { it.createdAt }
    }
}
