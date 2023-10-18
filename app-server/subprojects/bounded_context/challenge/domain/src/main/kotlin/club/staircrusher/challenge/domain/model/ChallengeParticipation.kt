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
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
