package club.staircrusher.challenge.domain.model

enum class ChallengeConditionAccessibilityType {
    BUILDING,
    PLACE
}

// ChallengeCondition 안의 값들은 AND 조건
// List<ChallengeCondition> 에서 각 Condition 은 OR 조건
data class ChallengeCondition(
    val addressMatches: List<String>,
    val accessibilityTypes: List<ChallengeConditionAccessibilityType>
)
