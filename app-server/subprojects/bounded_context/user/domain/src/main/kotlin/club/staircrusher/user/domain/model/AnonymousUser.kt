package club.staircrusher.user.domain.model

import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class AnonymousUser(
    @Id
    override val id: String,

    @Column(name = "scc_user_id", nullable = true)
    var identifiedUserId: String?,

    @Column(nullable = true)
    var convertedAt: Instant?,
) : User, TimeAuditingBaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnonymousUser

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "AnonymousUser(id='$id', identifiedUserId='$identifiedUserId', convertedAt='$convertedAt' createdAt='$createdAt', updatedAt='$updatedAt')"
    }
}
