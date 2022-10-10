package club.staircrusher.user.infra.adapter.out.persistence.sqldelight

import club.staircrusher.infra.persistence.sqldelight.query.user.User
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun User.toDomainModel() = club.staircrusher.user.domain.entity.User(
    id = id,
    nickname = nickname,
    encryptedPassword = encrypted_password,
    instagramId = instagram_id,
    createdAt = created_at.toInstant(),
)

fun club.staircrusher.user.domain.entity.User.toPersistenceModel() = User(
    id = id,
    nickname = nickname,
    encrypted_password = encryptedPassword,
    instagram_id = instagramId,
    created_at = createdAt.toLocalDateTime(),
)

private fun LocalDateTime.toInstant() = toInstant(ZoneOffset.UTC)
private fun Instant.toLocalDateTime() = atOffset(ZoneOffset.UTC).toLocalDateTime()
