package club.staircrusher.infra.persistence.sqldelight

import club.staircrusher.challenge.domain.model.ChallengeCondition
import club.staircrusher.infra.persistence.sqldelight.column_adapter.AccessibilityImageListStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.ClubQuestPurposeTypeStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.EntranceDoorTypeListStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.IntListToTextColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.ListToTextColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.LocationListToTextColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.PlaceCategoryStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.StairHeightLevelStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.StringListToTextColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.UserAuthProviderTypeStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.UserMobilityToolStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.migration.Accessibility_allowed_region
import club.staircrusher.infra.persistence.sqldelight.migration.Building_accessibility
import club.staircrusher.infra.persistence.sqldelight.migration.Challenge
import club.staircrusher.infra.persistence.sqldelight.migration.Club_quest
import club.staircrusher.infra.persistence.sqldelight.migration.Place
import club.staircrusher.infra.persistence.sqldelight.migration.Place_accessibility
import club.staircrusher.infra.persistence.sqldelight.migration.Scc_user
import club.staircrusher.infra.persistence.sqldelight.migration.User_auth_info
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.Transaction
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
            image_urlsAdapter = StringListToTextColumnAdapter,
            imagesAdapter = AccessibilityImageListStringColumnAdapter,
            floorsAdapter = IntListToTextColumnAdapter,
            stair_height_levelAdapter = StairHeightLevelStringColumnAdapter,
            entrance_door_typesAdapter = EntranceDoorTypeListStringColumnAdapter,
        ),
        building_accessibilityAdapter = Building_accessibility.Adapter(
            entrance_image_urlsAdapter = StringListToTextColumnAdapter,
            elevator_image_urlsAdapter = StringListToTextColumnAdapter,
            entrance_stair_height_levelAdapter = StairHeightLevelStringColumnAdapter,
            entrance_door_typesAdapter = EntranceDoorTypeListStringColumnAdapter,
            elevator_stair_height_levelAdapter = StairHeightLevelStringColumnAdapter,
            entrance_imagesAdapter = AccessibilityImageListStringColumnAdapter,
            elevator_imagesAdapter = AccessibilityImageListStringColumnAdapter,
        ),
        accessibility_allowed_regionAdapter = Accessibility_allowed_region.Adapter(
            boundary_verticesAdapter = LocationListToTextColumnAdapter,
        ),
        user_auth_infoAdapter = User_auth_info.Adapter(
            auth_provider_typeAdapter = UserAuthProviderTypeStringColumnAdapter,
        ),
        scc_userAdapter = Scc_user.Adapter(
            mobility_toolsAdapter = UserMobilityToolStringColumnAdapter,
        ),
        challengeAdapter = Challenge.Adapter(
            milestonesAdapter = IntListToTextColumnAdapter,
            conditionsAdapter = object : ListToTextColumnAdapter<ChallengeCondition>() {
                override fun convertElementToTextColumn(element: ChallengeCondition): String {
                    return objectMapper.writeValueAsString(element)
                }

                override fun convertElementFromTextColumn(text: String): ChallengeCondition {
                    return objectMapper.readValue(text)
                }

            }
        ),
        club_questAdapter = Club_quest.Adapter(
            purpose_typeAdapter = ClubQuestPurposeTypeStringColumnAdapter,
        )
    )

    val buildingQueries = scc.buildingQueries
    val placeQueries = scc.placeQueries
    val buildingAccessibilityQueries = scc.buildingAccessibilityQueries
    val buildingAccessibilityCommentQueries = scc.buildingAccessibilityCommentQueries
    val buildingAccessibilityUpvoteQueries = scc.buildingAccessibilityUpvoteQueries
    val placeAccessibilityQueries = scc.placeAccessibilityQueries
    val placeAccessibilityCommentQueries = scc.placeAccessibilityCommentQueries
    val placeAccessibilityUpvoteQueries = scc.placeAccessibilityUpvoteQueries
    val accessibilityRankQueries = scc.accessibilityRankQueries
    val userQueries = scc.userQueries
    val userAuthInfoQueries = scc.userAuthInfoQueries
    val clubQuestQueries = scc.clubQuestQueries
    val clubQuestTargetBuildingQueries = scc.clubQuestTargetBuildingQueries
    val clubQuestTargetPlaceQueries = scc.clubQuestTargetPlaceQueries
    val accessibilityAllowedRegionQueries = scc.accessibilityAllowedRegionQueries
    val challengeQueries = scc.challengeQueries
    val challengeContributionQueries = scc.challengeContributionQueries
    val challengeParticipationQueries = scc.challengeParticipationQueries
    val challengeRankQueries = scc.challengeRankQueries


    override fun <T> doInTransaction(block: Transaction<T>.() -> T): T {
        // FIXME: 다른 bounded context의 기능을 호출하기 때문에 nested transaction이 반드시 발생한다.
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
        // FIXME: 다른 bounded context의 기능을 호출하기 때문에 nested transaction이 반드시 발생한다.
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

    override fun doAfterCommit(block: () -> Unit) {
        block() // TODO: 올바르게 구현하기
    }
}

private val objectMapper = jacksonObjectMapper()
    .findAndRegisterModules()
    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
