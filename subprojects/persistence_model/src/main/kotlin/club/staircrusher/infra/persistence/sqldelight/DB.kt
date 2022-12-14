package club.staircrusher.infra.persistence.sqldelight

import club.staircrusher.infra.persistence.sqldelight.column_adapter.ListToTextColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.PlaceCategoryStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.StringListToTextColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.migration.Building_accessibility
import club.staircrusher.infra.persistence.sqldelight.migration.Club_quest
import club.staircrusher.infra.persistence.sqldelight.migration.Place
import club.staircrusher.infra.persistence.sqldelight.migration.Place_accessibility
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.Transaction
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.sql.DataSource

@Component
class DB(dataSource: DataSource) : TransactionManager {
    private val driver = SqlDelightJdbcDriver(dataSource)
    private val scc = scc(
        driver = driver,
        placeAdapter = Place.Adapter(
            categoryAdapter = PlaceCategoryStringColumnAdapter,
        ),
        place_accessibilityAdapter = Place_accessibility.Adapter(
            image_urlsAdapter = StringListToTextColumnAdapter
        ),
        building_accessibilityAdapter = Building_accessibility.Adapter(
            entrance_image_urlsAdapter = StringListToTextColumnAdapter,
            elevator_image_urlsAdapter = StringListToTextColumnAdapter,
        ),
        club_questAdapter = Club_quest.Adapter(
            target_buildingsAdapter = object : ListToTextColumnAdapter<ClubQuestTargetBuilding>() {
                override fun convertElementToTextColumn(element: ClubQuestTargetBuilding): String {
                    return objectMapper.writeValueAsString(element)
                }

                override fun convertElementFromTextColumn(text: String): ClubQuestTargetBuilding {
                    return objectMapper.readValue(text, ClubQuestTargetBuilding::class.java)
                }

            }
        )
    )

    val buildingQueries = scc.buildingQueries
    val placeQueries = scc.placeQueries
    val buildingAccessibilityQueries = scc.buildingAccessibilityQueries
    val buildingAccessibilityCommentQueries = scc.buildingAccessibilityCommentQueries
    val buildingAccessibilityUpvoteQueries = scc.buildingAccessibilityUpvoteQueries
    val placeAccessibilityQueries = scc.placeAccessibilityQueries
    val placeAccessibilityCommentQueries = scc.placeAccessibilityCommentQueries
    val userQueries = scc.userQueries
    val clubQuestQueries = scc.clubQuestQueries

    override fun <T> doInTransaction(block: Transaction<T>.() -> T): T {
        // FIXME: ?????? bounded context??? ????????? ???????????? ????????? nested transaction??? ????????? ????????????.
//        check(driver.isolationLevel == null) {
//            """
//            Since SCC does not allow nested transaction, isolationLevel saved in
//            thread local must be null.
//            """.trimIndent()
//        }
        return scc.transactionWithResult(noEnclosing = false) {
            SqlDelightTransaction(this).block()
        }
    }

    override fun <T> doInTransaction(
        isolationLevel: TransactionIsolationLevel,
        block: Transaction<T>.() -> T,
    ): T {
        // FIXME: ?????? bounded context??? ????????? ???????????? ????????? nested transaction??? ????????? ????????????.
//        check(driver.isolationLevel == null) {
//            """
//            Since SCC does not allow nested transaction, isolationLevel saved in
//            thread local must be null.
//            """.trimIndent()
//        }
        driver.isolationLevel = isolationLevel.toConnectionIsolationLevel()
        return try {
            scc.transactionWithResult(noEnclosing = false) {
                SqlDelightTransaction(this).block()
            }
        } finally {
            driver.isolationLevel = null
        }
    }
}

private val objectMapper = jacksonObjectMapper()
    .findAndRegisterModules()
    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
