package club.staircrusher.packaging.persistence

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("scc.database")
@Component
class SccDatabaseProperties {
    var driverClassName: String = ""
    var jdbcUrl: String = ""
    var username: String = ""
    var password: String = ""
}
