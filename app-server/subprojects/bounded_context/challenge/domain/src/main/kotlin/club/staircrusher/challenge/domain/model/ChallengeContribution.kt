package club.staircrusher.challenge.domain.model

import java.time.Instant

class ChallengeContribution(
    val id: String,
    val challengeId: String,
    val placeAccessibilityId: String?,
    val placeAccessibilityCommentId: String?,
    val buildingAccessibilityId: String?,
    val buildingAccessibilityCommentId: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChallengeContribution

        if (id != other.id) return false
        if (challengeId != other.challengeId) return false
        if (placeAccessibilityId != other.placeAccessibilityId) return false
        if (placeAccessibilityCommentId != other.placeAccessibilityCommentId) return false
        if (buildingAccessibilityId != other.buildingAccessibilityId) return false
        if (buildingAccessibilityCommentId != other.buildingAccessibilityCommentId) return false
        if (createdAt != other.createdAt) return false
        return updatedAt == other.updatedAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + challengeId.hashCode()
        result = 31 * result + (placeAccessibilityId?.hashCode() ?: 0)
        result = 31 * result + (placeAccessibilityCommentId?.hashCode() ?: 0)
        result = 31 * result + (buildingAccessibilityId?.hashCode() ?: 0)
        result = 31 * result + (buildingAccessibilityCommentId?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}
