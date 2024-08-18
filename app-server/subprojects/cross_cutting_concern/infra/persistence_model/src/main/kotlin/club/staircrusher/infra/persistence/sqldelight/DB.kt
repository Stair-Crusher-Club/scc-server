package club.staircrusher.infra.persistence.sqldelight

import app.cash.sqldelight.ColumnAdapter
import club.staircrusher.domain.server_event.ServerEventPayload
import club.staircrusher.domain.server_event.ServerEventType
import club.staircrusher.infra.persistence.sqldelight.migration.Server_event
import club.staircrusher.stdlib.di.annotation.Component
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
    )

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
