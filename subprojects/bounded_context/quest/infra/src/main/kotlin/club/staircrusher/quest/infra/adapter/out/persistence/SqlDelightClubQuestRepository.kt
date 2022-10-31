package club.staircrusher.quest.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.stdlib.di.annotation.Component

@Component
class SqlDelightClubQuestRepository(
    db: DB,
) : ClubQuestRepository {
    private val queries = db.clubQuestQueries

    override fun save(entity: ClubQuest): ClubQuest {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<ClubQuest>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): ClubQuest {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("User of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): ClubQuest? {
        return queries.findById(id = id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findAllOrderByCreatedAtDesc(): List<ClubQuest> {
        return queries.findAllOrderByCreatedAtDesc()
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun remove(clubQuestId: String) {
        return queries.remove(clubQuestId)
    }
}
