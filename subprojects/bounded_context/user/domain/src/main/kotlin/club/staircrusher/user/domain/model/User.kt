package club.staircrusher.user.domain.model

import java.time.Instant

data class User(
    val id: String,
    var nickname: String,
    var encryptedPassword: String,
    var instagramId: String?,
    val createdAt: Instant,
) {
    var deletedAt: Instant? = null // private으로 둘 방법이 없을까? 지금은 persistence_model 모듈에서 써야 해서 안 된다.

    val isDeleted: Boolean
        get() = deletedAt != null

    fun delete(deletedAt: Instant) {
        this.deletedAt = deletedAt
    }
}
