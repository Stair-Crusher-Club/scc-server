package club.staircrusher.user.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient
import java.time.Instant

@Entity
@Table(name = "scc_user")
class User(
    @Id
    val id: String,
    var nickname: String,
    @Deprecated("닉네임 로그인은 사라질 예정") var encryptedPassword: String?,
    var instagramId: String?,
    var email: String?, // FIXME: 레거시 계정이 모두 사라지면 non-nullable로 변경
    @Column(columnDefinition = "TEXT")
    @Convert(converter = UserMobilityToolListToTextAttributeConverter::class)
    val mobilityTools: MutableList<UserMobilityTool>,
    val createdAt: Instant,
) {
    private var deletedAt: Instant? = null

    @get:Transient
    val isDeleted: Boolean
        get() = deletedAt != null

    fun delete(deletedAt: Instant) {
        this.deletedAt = deletedAt
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "User(nickname='$nickname', id='$id', encryptedPassword=$encryptedPassword, instagramId=$instagramId, email=$email, mobilityTools=$mobilityTools, createdAt=$createdAt, deletedAt=$deletedAt, isDeleted=$isDeleted)"
    }
}
