package club.staircrusher.challenge.domain.model

import java.time.Instant

class Challenge(
    val id: String,
    val name: String,
    val isPublic: Boolean,
    val invitationCode: String?,
    val isComplete: Boolean,
    val startsAt: Instant,
    val endsAt: Instant?,
    val goals: List<Int>,
    val conditions: List<ChallengeCondition>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is Challenge && other.id == id
    }
}
