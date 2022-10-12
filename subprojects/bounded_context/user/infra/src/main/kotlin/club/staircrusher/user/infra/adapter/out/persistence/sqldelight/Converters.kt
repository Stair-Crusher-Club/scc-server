package club.staircrusher.user.infra.adapter.out.persistence.sqldelight

import club.staircrusher.infra.persistence.sqldelight.migration.Scc_user
import club.staircrusher.stdlib.time.toOffsetDateTime

fun Scc_user.toDomainModel() = club.staircrusher.user.domain.entity.User(
    id = id,
    nickname = nickname,
    encryptedPassword = encrypted_password,
    instagramId = instagram_id,
    createdAt = created_at.toInstant(),
)

fun club.staircrusher.user.domain.entity.User.toPersistenceModel() = Scc_user(
    id = id,
    nickname = nickname,
    encrypted_password = encryptedPassword,
    instagram_id = instagramId,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
)
