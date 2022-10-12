package club.staircrusher.infra.persistence.sqldelight

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import club.staircrusher.infra.persistence.sqldelight.migration.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.place.PlaceCategory
import javax.sql.DataSource

@Component
class DB(dataSource: DataSource) {
    private val placeCategoryStringColumnAdapter = object : ColumnAdapter<PlaceCategory, String> {
        override fun decode(databaseValue: String): PlaceCategory {
            return PlaceCategory.valueOf(databaseValue)
        }

        override fun encode(value: PlaceCategory): String = value.name
    }

    internal val scc = scc(
        driver = dataSource.asJdbcDriver(),
        placeAdapter = Place.Adapter(
            categoryAdapter = placeCategoryStringColumnAdapter,
        ),
    )

    val buildingQueries = scc.buildingQueries
    val placeQueries = scc.placeQueries
    val buildingAccessibilityQueries = scc.buildingAccessibilityQueries
    val buildingAccessibilityCommentQueries = scc.buildingAccessibilityCommentQueries
    val buildingAccessibilityUpvoteQueries = scc.buildingAccessibilityUpvoteQueries
    val placeAccessibilityQueries = scc.placeAccessibilityQueries
    val placeAccessibilityCommentQueries = scc.placeAccessibilityCommentQueries
    val userQueries = scc.userQueries
}
