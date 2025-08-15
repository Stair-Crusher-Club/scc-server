package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.place.PlaceCategory
import java.time.Instant

data class ChallengeQuest(
    val id: String,
    val title: String,
    val description: String,
    val condition: ChallengeQuestCondition,
) {
    fun isCompleted(progress: ChallengeQuestProgress): Boolean {
        return progress.completedCount >= condition.targetCount
    }
}

data class ChallengeQuestCondition(
    val targetCount: Int,
    val startsAt: Instant?, // null이면 챌린지 시작과 동시에 시작
    val endsAt: Instant?, // null이면 챌린지 종료와 함께 종료
    val actionConditions: List<ChallengeActionCondition.Type>, // 어떤 액션들을 대상으로 하는지
    val placeCategories: List<PlaceCategory>?, // null이면 모든 카테고리, 값이 있으면 특정 카테고리만
) {
    fun isSatisfied(
        actionType: ChallengeActionCondition.Type,
        placeCategory: PlaceCategory?,
        challengeStartsAt: Instant,
        challengeEndsAt: Instant?,
        contributionCreatedAt: Instant
    ): Boolean {
        // 퀘스트 기간 체크
        val questStartTime = startsAt ?: challengeStartsAt
        val questEndTime = endsAt ?: challengeEndsAt

        if (contributionCreatedAt.isBefore(questStartTime)) {
            return false
        }

        if (questEndTime != null && contributionCreatedAt.isAfter(questEndTime)) {
            return false
        }

        // 액션 타입 체크
        if (!actionConditions.contains(actionType)) {
            return false
        }

        // 장소 카테고리 체크
        if (placeCategories != null) {
            if (placeCategory == null || !placeCategories.contains(placeCategory)) {
                return false
            }
        }

        return true
    }
}
