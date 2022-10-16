package club.staircrusher.infra.persistence.sqldelight

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import club.staircrusher.infra.persistence.sqldelight.migration.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.Transaction
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.place.PlaceCategory
import javax.sql.DataSource

@Component
class DB(dataSource: DataSource) : TransactionManager, TransacterImpl(SqlDelightJdbcDriver(dataSource)) {
    private val placeCategoryStringColumnAdapter = object : ColumnAdapter<PlaceCategory, String> {
        override fun decode(databaseValue: String): PlaceCategory {
            return PlaceCategory.valueOf(databaseValue)
        }

        override fun encode(value: PlaceCategory): String = value.name
    }

    private val scc = scc(
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

    override fun <T> doInTransaction(block: Transaction<T>.() -> T): T {
        // FIXME: 다른 bounded context의 기능을 호출하기 때문에 nested transaction이 반드시 발생한다.
//        check(driver.isolationLevel == null) {
//            """
//            Since SCC does not allow nested transaction, isolationLevel saved in
//            thread local must be null.
//            """.trimIndent()
//        }
        return transactionWithResult(noEnclosing = false) {
            SqlDelightTransaction(this).block()
        }
    }

    override fun <T> doInTransaction(
        isolationLevel: TransactionIsolationLevel,
        block: Transaction<T>.() -> T,
    ): T {
        val driver = this.driver as SqlDelightJdbcDriver
        // FIXME: 다른 bounded context의 기능을 호출하기 때문에 nested transaction이 반드시 발생한다.
//        check(driver.isolationLevel == null) {
//            """
//            Since SCC does not allow nested transaction, isolationLevel saved in
//            thread local must be null.
//            """.trimIndent()
//        }
        driver.isolationLevel = isolationLevel.toConnectionIsolationLevel()
        return try {
            transactionWithResult(noEnclosing = false) {
                SqlDelightTransaction(this).block()
            }
        } finally {
            driver.isolationLevel = null
        }
    }
}
