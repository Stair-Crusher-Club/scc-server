package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.clock.SccClock
import java.time.Instant

class ChallengeQuestProgress(
    val questId: String,
    val contributions: MutableList<ChallengeQuestContribution>, // 이 퀘스트에 기여한 기여 정보들
) {
    var completedAt: Instant? = null
        protected set

    val completedCount: Int
        get() = contributions.size

    val isCompleted: Boolean
        get() = completedAt != null

    val contributionIds: List<String>
        get() = contributions.map { it.contributionId }

    fun addContribution(
        contributionId: String,
        actionType: ChallengeActionCondition.Type,
        contributedAt: Instant,
        quest: ChallengeQuest
    ) {
        if (!contributions.any { it.contributionId == contributionId }) {
            contributions.add(
                ChallengeQuestContribution(
                    contributionId = contributionId,
                    actionType = actionType,
                    contributedAt = contributedAt
                )
            )

            // 퀘스트가 완료되었으면 완료 시간 설정
            if (quest.isCompleted(this) && completedAt == null) {
                completedAt = SccClock.instant()
            }
        }
    }

    fun removeContribution(contributionId: String, quest: ChallengeQuest) {
        contributions.removeAll { it.contributionId == contributionId }

        // 퀘스트가 더 이상 완료되지 않으면 완료 시간 리셋
        if (!quest.isCompleted(this)) {
            completedAt = null
        }
    }


    companion object {
        fun create(questId: String): ChallengeQuestProgress {
            return ChallengeQuestProgress(
                questId = questId,
                contributions = mutableListOf()
            )
        }
    }
}
