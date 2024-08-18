package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.ChallengeContribution
import org.springframework.data.repository.CrudRepository

interface ChallengeContributionRepository : CrudRepository<ChallengeContribution, String> {
    fun findByUserId(userId: String): List<ChallengeContribution>
    fun findByChallengeId(challengeId: String): List<ChallengeContribution>
    fun findFirstByChallengeIdAndPlaceAccessibilityId(challengeId: String, placeAccessibilityId: String): ChallengeContribution?
    fun findByPlaceAccessibilityId(placeAccessibilityId: String): List<ChallengeContribution>
    fun findFirstByChallengeIdAndPlaceAccessibilityCommentId(challengeId: String, placeAccessibilityCommentId: String): ChallengeContribution?
    fun findFirstByChallengeIdAndBuildingAccessibilityId(challengeId: String, buildingAccessibilityId: String): ChallengeContribution?
    fun findByBuildingAccessibilityId(buildingAccessibilityId: String): List<ChallengeContribution>
    fun findFirstByChallengeIdAndBuildingAccessibilityCommentId(challengeId: String, buildingAccessibilityCommentId: String): ChallengeContribution?
    fun countByChallengeId(challengeId: String): Long
}
