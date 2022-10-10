package club.staircrusher.packaging.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
open class SccDatabaseConfig {
    @Bean
    open fun dataSource(sccDatabaseProperties: SccDatabaseProperties): DataSource {
        val config = HikariConfig()
        config.driverClassName = sccDatabaseProperties.driverClassName
        config.jdbcUrl = sccDatabaseProperties.jdbcUrl
        config.username = sccDatabaseProperties.username
        config.password = sccDatabaseProperties.password
        config.isAutoCommit = false
        return HikariDataSource(config)
    }
}
