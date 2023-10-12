package club.staircrusher.challenge.infra.adapter.out.persistence

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Suppress("TooManyFunctions")
@Component
class ChallengeContributionRepository(
    val db: DB
) : ChallengeContributionRepository {
    private val queries = db.challengeContributionQueries

    override fun findById(id: String): ChallengeContribution {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("ChallengeContribution of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): ChallengeContribution? {
        return queries.findById(id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByUserId(userId: String): List<ChallengeContribution> {
        return queries.findByUserId(userId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByUserIds(userIds: List<String>): List<ChallengeContribution> {
        return queries.findByUserIds(userIds)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByChallengeId(challengeId: String): List<ChallengeContribution> {
        return queries.findByChallengeId(challengeId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByChallengeIds(challengeIds: List<String>): List<ChallengeContribution> {
        return queries.findByChallengeIds(challengeIds)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByChallengeIdAndPlaceAccessibilityId(challengeId: String, placeAccessibilityId: String): ChallengeContribution? {
        return queries.findByChallengeIdAndPlaceAccessibilityId(challengeId, placeAccessibilityId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByChallengeIdAndPlaceAccessibilityCommentId(challengeId: String, placeAccessibilityCommentId: String): ChallengeContribution? {
        return queries.findByChallengeIdAndPlaceAccessibilityCommentId(challengeId, placeAccessibilityCommentId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByChallengeIdAndBuildingAccessibilityId(challengeId: String, buildingAccessibilityId: String): ChallengeContribution? {
        return queries.findByChallengeIdAndBuildingAccessibilityId(challengeId, buildingAccessibilityId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByChallengeIdAndBuildingAccessibilityCommentId(challengeId: String, buildingAccessibilityCommentId: String): ChallengeContribution? {
        return queries.findByChallengeIdAndBuildingAccessibilityCommentId(challengeId, buildingAccessibilityCommentId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun countByChallengeId(challengeId: String): Long {
        return queries.countByChallengeId(challengeId).executeAsOne()
    }

    override fun remove(contributionId: String) {
        queries.removeById(contributionId)
    }

    override fun save(entity: ChallengeContribution): ChallengeContribution {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<ChallengeContribution>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }
}
