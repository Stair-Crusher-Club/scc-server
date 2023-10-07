package club.staircrusher.challenge.domain.model

data class ChallengeAddress(
    val siDo: String,
    val siGunGu: String,
    val eupMyeonDong: String,
    val li: String,
    val roadName: String
) {
    fun contains(keyword: String): Boolean {
        return siDo.contains(keyword) ||
            siGunGu.contains(keyword) ||
            eupMyeonDong.contains(keyword) ||
            li.contains(keyword) ||
            roadName.contains(keyword)
    }
}
