package club.staircrusher.challenge.infra.adapter.out.persistence

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ChallengeContributionRepository(
    val db: DB
) : ChallengeContributionRepository {
    private val queries = db.challengeContributionQueries

    override fun findByUserId(userId: String): List<ChallengeContribution> {
        TODO("Not yet implemented")
    }

    override fun findByChallengeId(challengeId: String): List<ChallengeContribution> {
        TODO("Not yet implemented")
    }

    override fun countByChallengeId(challengeId: String): Int {
        TODO("Not yet implemented")
    }

    override fun remove(contributionId: String) {
        TODO("Not yet implemented")
    }

    override fun save(entity: ChallengeContribution): ChallengeContribution {
        TODO("Not yet implemented")
    }

    override fun saveAll(entities: Collection<ChallengeContribution>) {
        TODO("Not yet implemented")
    }

    override fun removeAll() {
        TODO("Not yet implemented")
    }

    override fun findById(id: String): ChallengeContribution {
        TODO("Not yet implemented")
    }

    override fun findByIdOrNull(id: String): ChallengeContribution? {
        TODO("Not yet implemented")
    }
}
