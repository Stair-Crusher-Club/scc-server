package club.staircrusher.quest.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.infra.persistence.sqldelight.migration.Club_quest
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetBuildingRepository
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.domain.model.ClubQuestSummary
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.time.toOffsetDateTime
import java.time.Instant

@Component
class SqlDelightClubQuestRepository(
    db: DB,
    private val clubQuestTargetBuildingRepository: ClubQuestTargetBuildingRepository,
) : ClubQuestRepository {
    private val clubQuestQueries = db.clubQuestQueries
    private val targetBuildingQueries = db.clubQuestTargetBuildingQueries
    private val targetPlaceQueries = db.clubQuestTargetPlaceQueries

    override fun save(entity: ClubQuest): ClubQuest {
        clubQuestQueries.save(entity.toPersistenceModel())
        clubQuestTargetBuildingRepository.saveAll(entity.targetBuildings)
        return entity
    }

    override fun saveAll(entities: Collection<ClubQuest>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        clubQuestQueries.removeAll()
    }

    override fun findById(id: String): ClubQuest {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("ClubQuest of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): ClubQuest? {
        return clubQuestQueries.findById(id = id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findAllOrderByCreatedAtDesc(): List<ClubQuest> {
        return clubQuestQueries.findAllOrderByCreatedAtDesc()
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findCursoredSummariesOrderByCreatedAtDesc(
        cursorCreatedAt: Instant,
        cursorId: String,
        limit: Int
    ): List<ClubQuestSummary> {
        return clubQuestQueries.findCursoredSummariesOrderByCreatedAtDesc(
            cursorCreatedAt = cursorCreatedAt.toOffsetDateTime(),
            cursorId = cursorId,
            limit = limit.toLong(),
        )
            .executeAsList()
            .map {
                ClubQuestSummary(
                    id = it.id,
                    name = it.name,
                    shortenedUrl = it.shortened_admin_url,
                    createdAt = it.created_at.toInstant(),
                )
            }
    }

    override fun remove(clubQuestId: String) {
        return clubQuestQueries.remove(clubQuestId)
    }

    private fun Club_quest.toDomainModel(): ClubQuest {
        return listOf(this)
            .toDomainModels()
            .first()
    }

    private fun List<Club_quest>.toDomainModels(): List<ClubQuest> {
        val clubQuestIds = this.map { it.id }
        val rawTargetBuildingsByClubQuestId = targetBuildingQueries.findByClubQuestIds(clubQuestIds)
            .executeAsList()
            .groupBy { it.club_quest_id }
        val rawTargetPlacesByTargetBuildingId = targetPlaceQueries.findByClubQuestIds(clubQuestIds)
            .executeAsList()
            .groupBy { it.target_building_id }
        return this.map { rawClubQuest ->
            val rawTargetBuildings = rawTargetBuildingsByClubQuestId[rawClubQuest.id] ?: emptyList()
            val targetBuildings = rawTargetBuildings.map { rawTargetBuilding ->
                val rawTargetPlaces = rawTargetPlacesByTargetBuildingId[rawTargetBuilding.id] ?: emptyList()
                val targetPlaces = rawTargetPlaces.map { it.toDomainModel() }
                rawTargetBuilding.toDomainModel(targetPlaces)
            }
            rawClubQuest.toDomainModel(targetBuildings)
        }
    }
}
