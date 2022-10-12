package club.staircrusher.infra.persistence.sqldelight

import mu.KotlinLogging
import org.flywaydb.core.Flyway
import javax.sql.DataSource

class OnlyOnceDatabaseMigrator(
    private val dataSource: DataSource,
) {
    private val flyway = Flyway.configure().dataSource(dataSource).load()
    private var finished = false

    fun migrate() {
        if (finished) {
            return
        }
        synchronized(this) {
            if (finished) {
                return
            }
            logger.info { "DB Migration started." }
            finished = true // at most once
            flyway.migrate()
            logger.info { "DB Migration finished." }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
