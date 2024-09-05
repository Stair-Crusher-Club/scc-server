package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.jpa.IntListToTextAttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Duration
import java.time.Instant

@Entity
class Challenge(
    @Id
    val id: String,
    val name: String,
    val isPublic: Boolean,
    val invitationCode: String?,
    val passcode: String?,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "TEXT")
    val crusherGroup: ChallengeCrusherGroup?,
    var isComplete: Boolean,
    val startsAt: Instant,
    val endsAt: Instant?,
    val goal: Int,
    @Convert(converter = IntListToTextAttributeConverter::class)
    val milestones: List<Int>,
    @Convert(converter = ChallengeConditionListToTextAttributeConverter::class)
    val conditions: List<ChallengeCondition>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val description: String,
) {
    fun getStatus(criteriaTime: Instant): ChallengeStatus {
        return when {
            criteriaTime < startsAt -> ChallengeStatus.UPCOMING
            endsAt != null && endsAt!! < criteriaTime -> ChallengeStatus.CLOSED
            else -> ChallengeStatus.IN_PROGRESS
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Challenge

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Challenge(id='$id', name='$name', isPublic=$isPublic, invitationCode=$invitationCode, " +
            "passcode=$passcode, crusherGroup=$crusherGroup, isComplete=$isComplete, startsAt=$startsAt, " +
            "endsAt=$endsAt, goal=$goal, milestones=$milestones, conditions=$conditions, createdAt=$createdAt, " +
            "updatedAt=$updatedAt, description='$description')"
    }

    companion object {
        // Instant.MAX 는 범위 초과로 1000년을 추가해서 쓴다.
        val MAX_TIME = Instant.EPOCH.plus(Duration.ofDays(365 * 1000))
        val MIN_TIME = Instant.EPOCH

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

            createRequest.conditions.forEach { condition ->
                check(condition.addressCondition?.rawEupMyeonDongs == null || condition.addressCondition.rawEupMyeonDongs.isNotEmpty()) {
                    "퀘스트 대상 지역은 전체이거나 최소 1곳 이상을 지정해야 합니다."
                }
                check(condition.actionCondition?.types == null || condition.actionCondition.types.isNotEmpty()) {
                    "퀘스트 대상 액션은 최소 1개 이상을 지정해야 합니다."
                }
            }

            return Challenge(
                id = EntityIdGenerator.generateRandom(),
                name = validateAndNormalizeString(createRequest.name),
                isPublic = createRequest.isPublic,
                invitationCode = invitationCode,
                passcode = passcode,
                crusherGroup = null,
                isComplete = false,
                startsAt = startsAt,
                endsAt = endsAt,
                goal = createRequest.goal,
                milestones = milestones,
                conditions = createRequest.conditions,
                createdAt = now,
                updatedAt = now,
                description = createRequest.description.trim(),
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
