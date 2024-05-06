package club.staircrusher.quest.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.stdlib.di.annotation.Component

@Component
class SqlDelightClubQuestTargetPlaceRepository(
    db: DB,
) : ClubQuestTargetPlaceRepository {
    private val queries = db.clubQuestTargetPlaceQueries

    override fun findByClubQuestIdAndPlaceId(clubQuestId: String, placeId: String): ClubQuestTargetPlace? {
        return queries.findByClubQuestIdAndPlaceId(clubQuestId = clubQuestId, placeId = placeId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByTargetBuildingId(targetBuildingId: String): List<ClubQuestTargetPlace> {
        return queries.findByTargetBuildingId(targetBuildingId = targetBuildingId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun removeByClubQuestId(clubQuestId: String) {
        queries.removeByClubQuestId(clubQuestId)
    }

    override fun save(entity: ClubQuestTargetPlace): ClubQuestTargetPlace {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<ClubQuestTargetPlace>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): ClubQuestTargetPlace {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("ClubQuestTargetPlace of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): ClubQuestTargetPlace? {
        return queries.findById(id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }
}
