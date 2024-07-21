package club.staircrusher.user.domain.model

import club.staircrusher.stdlib.clock.SccClock
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import java.time.Instant

@Entity
class UserAuthInfo(
    @Id
    val id: String,
    val userId: String,
    @Enumerated(EnumType.STRING)
    val authProviderType: UserAuthProviderType,
    val externalId: String,
    var externalRefreshToken: String,
    var externalRefreshTokenExpiresAt: Instant,
    val createdAt: Instant = SccClock.instant()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAuthInfo

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "UserAuthInfo(id='$id', userId='$userId', authProviderType=$authProviderType, externalId='$externalId', externalRefreshToken='$externalRefreshToken', externalRefreshTokenExpiresAt=$externalRefreshTokenExpiresAt, createdAt=$createdAt)"
    }
}
