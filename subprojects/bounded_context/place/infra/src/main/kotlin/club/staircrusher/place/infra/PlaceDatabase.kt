package club.staircrusher.place.infra

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import club.staircrusher.place.infra.db.Place
import club.staircrusher.place.infra.db.StairCrusherClub
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.place.PlaceCategory
import javax.sql.DataSource

@Component
class PlaceDatabase(dataSource: DataSource) {
    private val placeCategoryStringColumnAdapter = object : ColumnAdapter<PlaceCategory, String> {
        override fun decode(databaseValue: String): PlaceCategory {
            return PlaceCategory.valueOf(databaseValue)
        }

        override fun encode(value: PlaceCategory): String = value.name
    }

    private val stairCrusherClub = StairCrusherClub(
        driver = dataSource.asJdbcDriver(),
        placeAdapter = Place.Adapter(
            categoryAdapter = placeCategoryStringColumnAdapter,
        ),
    )

    val buildingQueries = stairCrusherClub.buildingQueries
    val placeQueries = stairCrusherClub.placeQueries
}