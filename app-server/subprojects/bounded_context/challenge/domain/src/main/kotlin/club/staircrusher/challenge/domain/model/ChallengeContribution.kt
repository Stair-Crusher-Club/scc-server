package club.staircrusher.challenge.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class ChallengeContribution(
    @Id
    val id: String,
    val userId: String,
    val challengeId: String,
    val placeAccessibilityId: String?,
    val placeAccessibilityCommentId: String?,
    val buildingAccessibilityId: String?,
    val buildingAccessibilityCommentId: String?,
    val placeReviewId: String?,
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

    override fun toString(): String {
        return "ChallengeContribution(id='$id', userId='$userId', challengeId='$challengeId', " +
            "placeAccessibilityId=$placeAccessibilityId, placeAccessibilityCommentId=$placeAccessibilityCommentId, " +
            "buildingAccessibilityId=$buildingAccessibilityId, " +
            "buildingAccessibilityCommentId=$buildingAccessibilityCommentId, placeReviewId=$placeReviewId, " +
            "createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}
