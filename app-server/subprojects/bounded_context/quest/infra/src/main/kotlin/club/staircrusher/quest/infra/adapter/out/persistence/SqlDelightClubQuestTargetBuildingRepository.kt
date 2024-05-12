package club.staircrusher.quest.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.infra.persistence.sqldelight.migration.Club_quest_target_building
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetBuildingRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.stdlib.di.annotation.Component

@Component
class SqlDelightClubQuestTargetBuildingRepository(
    db: DB,
    private val clubQuestTargetPlaceRepository: ClubQuestTargetPlaceRepository,
) : ClubQuestTargetBuildingRepository {
    private val queries = db.clubQuestTargetBuildingQueries

    override fun removeByClubQuestId(clubQuestId: String) {
        queries.removeByClubQuestId(clubQuestId)
    }

    override fun findByClubQuestIdAndBuildingId(clubQuestId: String, buildingId: String): ClubQuestTargetBuilding? {
        return queries.findByClubQuestIdAndBuildingId(clubQuestId = clubQuestId, buildingId = buildingId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun save(entity: ClubQuestTargetBuilding): ClubQuestTargetBuilding {
        queries.save(entity.toPersistenceModel())
        clubQuestTargetPlaceRepository.saveAll(entity.places)
        return entity
    }

    override fun saveAll(entities: Collection<ClubQuestTargetBuilding>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): ClubQuestTargetBuilding {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("ClubQuestTargetBuilding of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): ClubQuestTargetBuilding? {
        return queries.findById(id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    private fun Club_quest_target_building.toDomainModel(): ClubQuestTargetBuilding {
        val targetPlaces = clubQuestTargetPlaceRepository.findByTargetBuildingId(this.id)
        return this.toDomainModel(targetPlaces)
    }
}
