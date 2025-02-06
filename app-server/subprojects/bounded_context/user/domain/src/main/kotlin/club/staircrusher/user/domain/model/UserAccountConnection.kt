package club.staircrusher.user.domain.model

import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class UserAccountConnection(
    @Id
    val id: String,

    val identifiedUserAccountId: String,

    val anonymousUserAccountId: String,
) : TimeAuditingBaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAccountConnection

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "UserAccountConnection(id='$id', identifiedUserAccountId='$identifiedUserAccountId', anonymousUserAccountId='$anonymousUserAccountId' createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}
