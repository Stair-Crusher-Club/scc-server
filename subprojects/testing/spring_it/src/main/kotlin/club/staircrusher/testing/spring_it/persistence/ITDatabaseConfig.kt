package club.staircrusher.testing.spring_it.persistence

import club.staircrusher.infra.persistence.sqldelight.OnlyOnceDatabaseMigrator
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration(proxyBeanMethods = false)
open class ITDatabaseConfig {
    @Bean
    open fun databaseMigrator(dataSource: DataSource) = ApplicationRunner {
        val onlyOnceDatabaseMigrator = OnlyOnceDatabaseMigrator(dataSource)
        onlyOnceDatabaseMigrator.migrate()
    }
}
