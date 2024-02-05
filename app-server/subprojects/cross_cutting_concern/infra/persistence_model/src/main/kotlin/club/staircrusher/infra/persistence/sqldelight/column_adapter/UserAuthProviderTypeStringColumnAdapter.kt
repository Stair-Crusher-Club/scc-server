package club.staircrusher.infra.persistence.sqldelight.column_adapter

import app.cash.sqldelight.ColumnAdapter
import club.staircrusher.user.domain.model.UserAuthProviderType

object UserAuthProviderTypeStringColumnAdapter : ColumnAdapter<UserAuthProviderType, String> {
    override fun decode(databaseValue: String): UserAuthProviderType {
        return UserAuthProviderType.valueOf(databaseValue)
    }

    override fun encode(value: UserAuthProviderType): String = value.name
}
