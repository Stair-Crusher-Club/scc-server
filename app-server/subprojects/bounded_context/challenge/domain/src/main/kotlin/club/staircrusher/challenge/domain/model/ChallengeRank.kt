package club.staircrusher.challenge.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class ChallengeRank(
    @Id
    val id: String,
    val challengeId: String,
    val userId: String,
    var contributionCount: Long,
    var rank: Long,
    var createdAt: Instant,
    var updatedAt: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChallengeRank

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ChallengeRank(id='$id', challengeId='$challengeId', userId='$userId', " +
            "contributionCount=$contributionCount, rank=$rank, createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}
