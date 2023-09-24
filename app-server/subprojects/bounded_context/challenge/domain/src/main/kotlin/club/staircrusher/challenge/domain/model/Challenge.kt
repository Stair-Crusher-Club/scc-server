package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import java.time.Instant

class Challenge(
    val id: String,
    val name: String,
    val isPublic: Boolean,
    val invitationCode: String?,
    val passcode: String?,
    val isComplete: Boolean,
    val startsAt: Instant,
    val endsAt: Instant?,
    val goal: Int,
    val milestones: List<Int>,
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

    companion object {
        fun of(createRequest: CreateChallengeRequest): Challenge {
            val now = SccClock.instant()

            val invitationCode = createRequest.invitationCode?.let { validateAndNormalizeString(it) }
            val passcode = createRequest.passcode?.let { validateAndNormalizeString(it) }

            if (createRequest.isPublic) {
                check(invitationCode == null) {
                    "공개 챌린지는 초대 코드가 없어야 합니다."
                }
            } else {
                check(invitationCode != null) {
                    "비공개 챌린지는 초대 코드가 있어야 합니다."
                }
            }

            val startsAt = Instant.ofEpochMilli(createRequest.startsAtMillis)
            val endsAt = createRequest.endsAtMillis?.let { Instant.ofEpochMilli(it) }
            if (endsAt != null) {
                check(startsAt < endsAt) { "시작 시각은 종료 시각보다 빨라야 합니다." }
            }

            val milestones = createRequest.milestones.sorted()
            if (milestones.isNotEmpty()) {
                check(milestones.last() < createRequest.goal) {
                    "목표는 마일스톤보다 커야 합니다."
                }
            }

            return Challenge(
                id = EntityIdGenerator.generateRandom(),
                name = validateAndNormalizeString(createRequest.name),
                isPublic = createRequest.isPublic,
                invitationCode = invitationCode,
                passcode = passcode,
                isComplete = false,
                startsAt = startsAt,
                endsAt = endsAt,
                goal = createRequest.goal,
                milestones = milestones,
                conditions = emptyList(), // TODO
                createdAt = now,
                updatedAt = now,
            )
        }

        private fun validateAndNormalizeString(value: String): String {
            return value
                .trim()
                .also {
                    check(it.isNotBlank()) { "값이 공백이면 안 됩니다." }
                }
        }
    }
}
