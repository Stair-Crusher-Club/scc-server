package club.staircrusher.accessibility.domain.model

data class AccessibilityRanking(
    val id: String,
    val userId: String,
    val conquestCount: Int,
    val rank: Long,
)
