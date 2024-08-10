package club.staircrusher.infra.persistence.sqldelight

import app.cash.sqldelight.ColumnAdapter
import club.staircrusher.challenge.domain.model.ChallengeCondition
import club.staircrusher.domain.server_event.ServerEventPayload
import club.staircrusher.domain.server_event.ServerEventType
import club.staircrusher.infra.persistence.sqldelight.column_adapter.AccessibilityImageListStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.EntranceDoorTypeListStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.IntListToTextColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.ListToTextColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.LocationListToTextColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.StairHeightLevelStringColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.column_adapter.StringListToTextColumnAdapter
import club.staircrusher.infra.persistence.sqldelight.migration.Accessibility_allowed_region
import club.staircrusher.infra.persistence.sqldelight.migration.Accessibility_image_face_blurring_history
import club.staircrusher.infra.persistence.sqldelight.migration.Building_accessibility
import club.staircrusher.infra.persistence.sqldelight.migration.Challenge
import club.staircrusher.infra.persistence.sqldelight.migration.External_accessibility
import club.staircrusher.infra.persistence.sqldelight.migration.Place_accessibility
import club.staircrusher.infra.persistence.sqldelight.migration.Server_event
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javax.sql.DataSource

@Component
class DB(dataSource: DataSource) {
    private val driver = SqlDelightJdbcDriver(dataSource)
    private val scc = scc(
        driver = driver,
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
        external_accessibilityAdapter = External_accessibility.Adapter(
            categoryAdapter = object : ColumnAdapter<ExternalAccessibilityCategory, String> {
                override fun decode(databaseValue: String): ExternalAccessibilityCategory {
                    return ExternalAccessibilityCategory.valueOf(databaseValue)
                }

                override fun encode(value: ExternalAccessibilityCategory): String = value.name
            },
        ),
        server_eventAdapter = Server_event.Adapter(
            typeAdapter = object : ColumnAdapter<ServerEventType, String> {
                override fun decode(databaseValue: String): ServerEventType {
                    return ServerEventType.valueOf(databaseValue)
                }

                override fun encode(value: ServerEventType): String {
                    return value.name
                }
            },
            payloadAdapter = object : ColumnAdapter<ServerEventPayload, String> {
                override fun decode(databaseValue: String): ServerEventPayload {
                    return objectMapper.readValue(databaseValue)
                }

                override fun encode(value: ServerEventPayload): String {
                    return objectMapper.writeValueAsString(value)
                }
            }
        ),
        accessibility_image_face_blurring_historyAdapter = Accessibility_image_face_blurring_history.Adapter(
            original_image_urlsAdapter = StringListToTextColumnAdapter,
            blurred_image_urlsAdapter = StringListToTextColumnAdapter,
            detected_people_countsAdapter = IntListToTextColumnAdapter,
        ),
    )

    val accessibilityImageFaceBlurringHistoryQueries = scc.accessibilityImageFaceBlurringHistoryQueries
    val buildingAccessibilityQueries = scc.buildingAccessibilityQueries
    val buildingAccessibilityCommentQueries = scc.buildingAccessibilityCommentQueries
    val buildingAccessibilityUpvoteQueries = scc.buildingAccessibilityUpvoteQueries
    val placeAccessibilityQueries = scc.placeAccessibilityQueries
    val placeAccessibilityCommentQueries = scc.placeAccessibilityCommentQueries
    val placeAccessibilityUpvoteQueries = scc.placeAccessibilityUpvoteQueries
    val accessibilityRankQueries = scc.accessibilityRankQueries
    val accessibilityAllowedRegionQueries = scc.accessibilityAllowedRegionQueries
    val challengeQueries = scc.challengeQueries
    val challengeContributionQueries = scc.challengeContributionQueries
    val challengeParticipationQueries = scc.challengeParticipationQueries
    val challengeRankQueries = scc.challengeRankQueries
    val externalAccessibilityQueries = scc.externalAccessibilityQueries
    val serverEventQueries = scc.serverEventQueries
}

private val objectMapper = jacksonObjectMapper()
    .findAndRegisterModules()
    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
