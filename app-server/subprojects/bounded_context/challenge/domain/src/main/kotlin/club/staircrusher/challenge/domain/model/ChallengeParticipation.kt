package club.staircrusher.challenge.domain.model

import java.time.Instant

class ChallengeParticipation(
    val id: String,
    val challengeId: String,
    val userId: String,
    val createdAt: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChallengeParticipation

        if (id != other.id) return false
        if (challengeId != other.challengeId) return false
        if (userId != other.userId) return false
        return createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + challengeId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}
