package club.staircrusher.infra.persistence.sqldelight

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import mu.KotlinLogging
import javax.sql.DataSource

class OnlyOnceDatabaseMigrator(
    private val dataSource: DataSource,
) {
    private var finished = false

    fun migrate(fromVersion: Int = 0) {
        if (finished) {
            return
        }
        synchronized(this) {
            if (finished) {
                return
            }
            logger.info { "DB Migration started." }
            finished = true // at most once
            StairCrusherClub.Schema.migrate(
                driver = dataSource.asJdbcDriver(),
                oldVersion = fromVersion,
                newVersion = StairCrusherClub.Schema.version,
            )
            logger.info { "DB Migration finished." }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
