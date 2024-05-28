package club.staircrusher.user.domain.model

data class ClientVersionStatus(
    val status: Status,
    val message: String?,
) {
    enum class Status {
        STABLE,
        UPGRADE_RECOMMENDED,
        UPGRADE_NEEDED,
    }
}
