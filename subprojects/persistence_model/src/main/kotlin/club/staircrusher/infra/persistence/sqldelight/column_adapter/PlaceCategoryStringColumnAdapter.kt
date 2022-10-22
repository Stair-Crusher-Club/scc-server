package club.staircrusher.infra.persistence.sqldelight.column_adapter

import app.cash.sqldelight.ColumnAdapter
import club.staircrusher.stdlib.place.PlaceCategory

object PlaceCategoryStringColumnAdapter : ColumnAdapter<PlaceCategory, String> {
    override fun decode(databaseValue: String): PlaceCategory {
        return PlaceCategory.valueOf(databaseValue)
    }

    override fun encode(value: PlaceCategory): String = value.name
}
