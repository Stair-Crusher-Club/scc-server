package club.staircrusher.challenge.domain.model

import java.time.Instant

class ChallengeContribution(
    val id: String,
    val userId: String,
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
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
