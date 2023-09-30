package club.staircrusher.challenge.domain.model

// ChallengeCondition 안의 값들은 AND 조건
// List<ChallengeCondition> 에서 각 Condition 은 OR 조건
data class ChallengeCondition(
    val addressCondition: ChallengeAddressCondition?,
    val actionCondition: ChallengeActionCondition?
) {
    @Suppress("ReturnCount")
    fun isSatisfied(
        address: ChallengeAddress,
        actionType: ChallengeActionCondition.Type
    ): Boolean {
        if (addressCondition?.isSatisfied(address) == false) return false
        if (actionCondition?.isSatisfied(actionType) == false) return false
        return true
    }
}

data class ChallengeAddressCondition(
    val keywords: List<String>
) {
    fun isSatisfied(address: ChallengeAddress): Boolean {
        return keywords.all { address.contains(it) }
    }
}

data class ChallengeActionCondition(val types: List<Type>) {
    enum class Type {
        BUILDING_ACCESSIBILITY,
        BUILDING_ACCESSIBILITY_COMMENT,
        PLACE_ACCESSIBILITY,
        PLACE_ACCESSIBILITY_COMMENT,
    }

    fun isSatisfied(type: Type): Boolean {
        return types.contains(type)
    }
}
