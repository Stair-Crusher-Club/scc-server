package club.staircrusher.user.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.migration.Scc_user
import club.staircrusher.infra.persistence.sqldelight.migration.User_auth_info
import club.staircrusher.stdlib.time.toOffsetDateTime
import club.staircrusher.user.domain.model.UserAuthInfo

fun Scc_user.toDomainModel() = club.staircrusher.user.domain.model.User(
    id = id,
    nickname = nickname,
    encryptedPassword = encrypted_password,
    instagramId = instagram_id,
    createdAt = created_at.toInstant(),
).apply {
    deletedAt = deleted_at?.toInstant()
}

fun club.staircrusher.user.domain.model.User.toPersistenceModel() = Scc_user(
    id = id,
    nickname = nickname,
    encrypted_password = encryptedPassword,
    instagram_id = instagramId,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
    deleted_at = deletedAt?.toOffsetDateTime(),
)

fun User_auth_info.toDomainModel() = UserAuthInfo(
    id = id,
    userId = user_id,
    authProviderType = auth_provider_type,
    externalId = external_id,
    externalRefreshToken = external_refresh_token,
    externalRefreshTokenExpiresAt = external_refresh_token_expires_at.toInstant(),
    createdAt = created_at.toInstant(),
)

fun UserAuthInfo.toPersistenceModel() = User_auth_info(
    id = id,
    user_id = userId,
    auth_provider_type = authProviderType,
    external_id = externalId,
    external_refresh_token = externalRefreshToken,
    external_refresh_token_expires_at = externalRefreshTokenExpiresAt.toOffsetDateTime(),
    created_at = createdAt.toOffsetDateTime(),
)
