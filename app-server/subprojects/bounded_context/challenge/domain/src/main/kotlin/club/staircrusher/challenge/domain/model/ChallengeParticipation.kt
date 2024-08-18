package club.staircrusher.challenge.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class ChallengeParticipation(
    @Id
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

    override fun toString(): String {
        return "ChallengeParticipation(id='$id', challengeId='$challengeId', userId='$userId', createdAt=$createdAt)"
    }
}
