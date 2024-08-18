package club.staircrusher.infra.persistence.sqldelight

import club.staircrusher.stdlib.di.annotation.Component
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.sql.DataSource

@Component
class DB(dataSource: DataSource) {
    private val driver = SqlDelightJdbcDriver(dataSource)
    private val scc = scc(
        driver = driver,
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
