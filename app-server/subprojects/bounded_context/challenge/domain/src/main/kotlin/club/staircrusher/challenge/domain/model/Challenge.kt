package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.SccDomainException
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
    var name: String,
    var isPublic: Boolean,
    var invitationCode: String?,
    var passcode: String?,
    var companyName: String?,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "TEXT")
    var crusherGroup: ChallengeCrusherGroup?,
    var isComplete: Boolean,
    var startsAt: Instant,
    var endsAt: Instant?,
    var goal: Int,
    @Convert(converter = IntListToTextAttributeConverter::class)
    var milestones: List<Int>,
    @Convert(converter = ChallengeConditionListToTextAttributeConverter::class)
    var conditions: List<ChallengeCondition>,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "TEXT")
    var quests: List<ChallengeQuest>?,
    val createdAt: Instant,
    var updatedAt: Instant,
    var description: String,
) {
    fun getStatus(criteriaTime: Instant): ChallengeStatus {
        return when {
            criteriaTime < startsAt -> ChallengeStatus.UPCOMING
            endsAt != null && endsAt!! < criteriaTime -> ChallengeStatus.CLOSED
            else -> ChallengeStatus.IN_PROGRESS
        }
    }

    fun update(updateRequest: UpdateChallengeRequest) {
        val endsAt = updateRequest.endsAt
        if (endsAt != null && startsAt.isAfter(endsAt)) {
            throw SccDomainException("시작 시각은 종료시각보다 빨라야 합니다.")
        }

        // 시작된 챌린지의 퀘스트는 수정할 수 없음
        if (updateRequest.quests != null) {
            if (getStatus(SccClock.instant()) != ChallengeStatus.UPCOMING) {
                throw SccDomainException("시작된 챌린지의 퀘스트는 수정할 수 없습니다.")
            }
        }

        this.name = validateAndNormalizeString(updateRequest.name)
        this.crusherGroup = updateRequest.crusherGroup
        this.endsAt = endsAt
        this.description = updateRequest.description.trim()
        updateRequest.quests?.let { this.quests = it }
        this.updatedAt = SccClock.instant()
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
            "passcode=$passcode, companyName=$companyName, crusherGroup=$crusherGroup, isComplete=$isComplete, startsAt=$startsAt, " +
            "endsAt=$endsAt, goal=$goal, milestones=$milestones, conditions=$conditions, quests=$quests, createdAt=$createdAt, " +
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
            val companyName = createRequest.companyName?.let { validateAndNormalizeString(it) }
            val startsAt = Instant.ofEpochMilli(createRequest.startsAtMillis)
            val endsAt = createRequest.endsAtMillis?.let { Instant.ofEpochMilli(it) }
            val milestones = createRequest.milestones.sorted()
            validate(invitationCode, createRequest.isPublic, startsAt, endsAt, createRequest.goal, milestones, createRequest.conditions)

            return Challenge(
                id = EntityIdGenerator.generateRandom(),
                name = validateAndNormalizeString(createRequest.name),
                isPublic = createRequest.isPublic,
                invitationCode = invitationCode,
                passcode = passcode,
                companyName = companyName,
                crusherGroup = createRequest.crusherGroup,
                isComplete = false,
                startsAt = startsAt,
                endsAt = endsAt,
                goal = createRequest.goal,
                milestones = milestones,
                conditions = createRequest.conditions,
                quests = createRequest.quests ?: emptyList(),
                createdAt = now,
                updatedAt = now,
                description = createRequest.description.trim(),
            )
        }

        private fun validate(
            invitationCode: String?,
            isPublic: Boolean,
            startsAt: Instant,
            endsAt: Instant?,
            goal: Int,
            milestones: List<Int>,
            conditions: List<ChallengeCondition>,
        ) {
            if (isPublic) {
                require(invitationCode == null) {
                    "공개 챌린지는 초대 코드가 없어야 합니다."
                }
            } else {
                require(invitationCode != null) {
                    "비공개 챌린지는 초대 코드가 있어야 합니다."
                }
            }

            if (endsAt != null) {
                require(startsAt < endsAt) { "시작 시각은 종료 시각보다 빨라야 합니다." }
            }

            if (milestones.isNotEmpty()) {
                require(milestones.last() < goal) {
                    "목표는 마일스톤보다 커야 합니다."
                }
            }

            conditions.forEach { condition ->
                require(condition.addressCondition?.rawEupMyeonDongs == null || condition.addressCondition.rawEupMyeonDongs.isNotEmpty()) {
                    "퀘스트 대상 지역은 전체이거나 최소 1곳 이상을 지정해야 합니다."
                }
                require(condition.actionCondition?.types == null || condition.actionCondition.types.isNotEmpty()) {
                    "퀘스트 대상 액션은 최소 1개 이상을 지정해야 합니다."
                }
            }
        }

        private fun validateAndNormalizeString(value: String): String {
            return value
                .trim()
                .also {
                    require(it.isNotBlank()) { "값이 공백이면 안 됩니다." }
                }
        }
    }
}
