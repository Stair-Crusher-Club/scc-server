package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.place.PlaceCategory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
class ChallengeParticipation(
    @Id
    val id: String,
    val challengeId: String,
    val userId: String,
    val participantName: String?,
    val companyName: String?,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "TEXT")
    var questProgresses: List<ChallengeQuestProgress>?,
    val createdAt: Instant,
) {
    init {
        require((participantName != null) == (companyName != null)) {
            "participantName과 companyName은 같이 설정되거나 같이 설정되지 않아야 합니다. $this"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChallengeParticipation
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun updateQuestProgress(
        challengeQuests: List<ChallengeQuest>,
        contributionId: String,
        actionType: ChallengeActionCondition.Type,
        placeCategory: PlaceCategory?,
        challengeStartsAt: Instant,
        challengeEndsAt: Instant?,
        contributionCreatedAt: Instant
    ) {
        challengeQuests
            .filter { it.condition.isSatisfied(actionType, placeCategory, challengeStartsAt, challengeEndsAt, contributionCreatedAt) }
            .forEach { updateSingleQuestProgress(it, contributionId, actionType, contributionCreatedAt) }
    }

    private fun updateSingleQuestProgress(
        quest: ChallengeQuest,
        contributionId: String,
        actionType: ChallengeActionCondition.Type,
        contributedAt: Instant,
    ) {
        // Lazy initialization: 퀘스트 진행도가 없으면 생성
        val progress = getOrInitializeQuestProgress(quest.id)
        progress.addContribution(contributionId, actionType, contributedAt, quest)
    }

    private fun getOrInitializeQuestProgress(questId: String): ChallengeQuestProgress {
        val currentProgresses = questProgresses ?: emptyList()
        val existingProgress = currentProgresses.find { it.questId == questId }
        return if (existingProgress == null) {
            val newProgress = ChallengeQuestProgress.create(questId)
            questProgresses = currentProgresses + newProgress
            newProgress
        } else {
            existingProgress
        }
    }

    fun removeQuestProgress(contributionId: String, challengeQuests: List<ChallengeQuest>) {
        questProgresses?.forEach { progress ->
            val quest = challengeQuests.find { it.id == progress.questId }
            if (quest != null) {
                progress.removeContribution(contributionId, quest)
            }
        }
    }

    override fun toString(): String {
        return "ChallengeParticipation(id='$id', challengeId='$challengeId', userId='$userId', participantName=$participantName, companyName=$companyName, questProgresses=$questProgresses, createdAt=$createdAt)"
    }
}
