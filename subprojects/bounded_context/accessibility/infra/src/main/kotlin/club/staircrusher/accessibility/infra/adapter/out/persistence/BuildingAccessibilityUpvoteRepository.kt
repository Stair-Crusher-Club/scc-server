package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class BuildingAccessibilityUpvoteRepository(
    db: DB,
) : BuildingAccessibilityUpvoteRepository {
    private val queries = db.buildingAccessibilityUpvoteQueries

    override fun save(entity: BuildingAccessibilityUpvote): BuildingAccessibilityUpvote {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<BuildingAccessibilityUpvote>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): BuildingAccessibilityUpvote {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("BuildingAccessibilityComment of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): BuildingAccessibilityUpvote? {
        return queries.buildingAccessibilityUpvoteFindById(id = id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findExistingUpvote(
        userId: String,
        buildingAccessibility: BuildingAccessibility
    ): BuildingAccessibilityUpvote? {
        return queries.findByUserAndBuildingAccessibilityAndNotDeleted(
            userId = userId,
            buildingAccessibilityId = buildingAccessibility.id,
        ).executeAsOneOrNull()?.toDomainModel()
    }

    override fun getTotalUpvoteCountOfBuildingAccessibility(buildingAccessibilityId: String): Int {
        return queries.getTotalUpvoteCountOfBuildingAccessibility(buildingAccessibilityId = buildingAccessibilityId)
            .executeAsOne()
            .toInt()
    }
}
