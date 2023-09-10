package club.staircrusher.challenge.infra.adapter.out.persistence

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ChallengeRepository(
    val db: DB
) : ChallengeRepository {
    private val queries = db.challengeQueries

    override fun findByChallengeId(challengeId: String): Challenge? {
        queries.findById(challengeId).executeAsOneOrNull().
        TODO("Not yet implemented")
    }

    override fun findByChallengeIds(challengeIds: Collection<String>): List<Challenge> {
        TODO("Not yet implemented")
    }

    override fun countAll(): Int {
        TODO("Not yet implemented")
    }

    override fun remove(id: String) {
        TODO("Not yet implemented")
    }

    override fun save(entity: Challenge): Challenge {
        TODO("Not yet implemented")
    }

    override fun saveAll(entities: Collection<Challenge>) {
        TODO("Not yet implemented")
    }

    override fun removeAll() {
        TODO("Not yet implemented")
    }

    override fun findById(id: String): Challenge {
        TODO("Not yet implemented")
    }

    override fun findByIdOrNull(id: String): Challenge? {
        TODO("Not yet implemented")
    }
}
