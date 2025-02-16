package club.staircrusher.user.domain.model

import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient
import java.time.Instant

@Entity
@Table(name = "scc_user")
class UserProfile(
    @Id
    val id: String,
    var userAccountId: String?,
    var nickname: String,
    @Deprecated("닉네임 로그인은 사라질 예정") var encryptedPassword: String?,
    var instagramId: String?,
    var email: String?,
    @Column(columnDefinition = "TEXT")
    @Convert(converter = UserMobilityToolListToTextAttributeConverter::class)
    var mobilityTools: List<UserMobilityTool>,
    var pushToken: String? = null,
) : TimeAuditingBaseEntity() {
    var deletedAt: Instant? = null

    @get:Transient
    val isDeleted: Boolean
        get() = deletedAt != null

    fun delete(deletedAt: Instant) {
        this.email = null
        this.deletedAt = deletedAt
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserProfile

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "UserProfile(nickname='$nickname', id='$id', userAccountId='$userAccountId', encryptedPassword=$encryptedPassword, instagramId=$instagramId, email=$email, mobilityTools=$mobilityTools, createdAt=$createdAt, updatedAt=$updatedAt, deletedAt=$deletedAt, isDeleted=$isDeleted)"
    }
}
