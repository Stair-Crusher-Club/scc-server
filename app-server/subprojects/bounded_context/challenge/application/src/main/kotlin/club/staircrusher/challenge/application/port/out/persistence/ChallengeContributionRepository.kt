package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ChallengeContributionRepository : EntityRepository<ChallengeContribution, String> {
    fun findByUserId(userId: String): List<ChallengeContribution>
    fun findByUserIds(userIds: List<String>): List<ChallengeContribution>
    fun findByChallengeId(challengeId: String): List<ChallengeContribution>
    fun findByChallengeIds(challengeIds: List<String>): List<ChallengeContribution>
    fun findByChallengeIdAndPlaceAccessibilityId(challengeId: String, placeAccessibilityId: String): ChallengeContribution?
    fun findByChallengeIdAndPlaceAccessibilityCommentId(challengeId: String, placeAccessibilityCommentId: String): ChallengeContribution?
    fun findByChallengeIdAndBuildingAccessibilityId(challengeId: String, buildingAccessibilityId: String): ChallengeContribution?
    fun findByChallengeIdAndBuildingAccessibilityCommentId(challengeId: String, buildingAccessibilityCommentId: String): ChallengeContribution?
    fun countByChallengeId(challengeId: String): Long
    fun remove(contributionId: String)
}
