package club.staircrusher.infra.persistence.sqldelight.column_adapter

import app.cash.sqldelight.ColumnAdapter
import club.staircrusher.accessibility.domain.model.StairHeightLevel

object StairHeightLevelStringColumnAdapter : ColumnAdapter<StairHeightLevel, String> {
    override fun decode(databaseValue: String): StairHeightLevel {
        return StairHeightLevel.valueOf(databaseValue)
    }

    override fun encode(value: StairHeightLevel): String = value.name
}
