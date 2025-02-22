package club.staircrusher.user.domain.model

import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Transient
import java.time.Instant

@Entity
class UserAccount(
    @Id
    val id: String,

    @Enumerated(EnumType.STRING)
    val accountType: UserAccountType,
) : TimeAuditingBaseEntity() {
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

        other as UserAccount

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "UserAccount(id='$id', accountType=$accountType, createdAt=$createdAt, updatedAt=$updatedAt, deletedAt=$deletedAt, isDeleted=$isDeleted)"
    }
}
