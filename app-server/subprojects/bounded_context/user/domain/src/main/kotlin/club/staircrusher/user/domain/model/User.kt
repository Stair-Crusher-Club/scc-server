package club.staircrusher.user.domain.model

import java.time.Instant

data class User(
    val id: String,
    var nickname: String,
    @Deprecated("닉네임 로그인은 사라질 예정") var encryptedPassword: String?,
    var instagramId: String?,
    var email: String?,
    val mobilityTools: MutableList<UserMobilityTool>,
    val createdAt: Instant,
) {
    var deletedAt: Instant? = null // private으로 둘 방법이 없을까? 지금은 persistence_model 모듈에서 써야 해서 안 된다.

    val isDeleted: Boolean
        get() = deletedAt != null

    fun delete(deletedAt: Instant) {
        this.email = null
        this.deletedAt = deletedAt
    }
}
