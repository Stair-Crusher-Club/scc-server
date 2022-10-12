package club.staircrusher.packaging.persistence

import club.staircrusher.infra.persistence.sqldelight.OnlyOnceDatabaseMigrator
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
open class SccDatabaseConfig {
    @Bean
    open fun databaseMigrator(dataSource: DataSource) = ApplicationRunner {
        val onlyOnceDatabaseMigrator = OnlyOnceDatabaseMigrator(dataSource)
        onlyOnceDatabaseMigrator.migrate()
    }
}
