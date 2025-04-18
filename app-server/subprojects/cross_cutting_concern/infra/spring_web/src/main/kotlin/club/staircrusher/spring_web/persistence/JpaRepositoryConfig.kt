package club.staircrusher.spring_web.persistence

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EntityScan("club.staircrusher")
@EnableJpaRepositories(
    basePackages = ["club.staircrusher"],
    transactionManagerRef = "sccPlatformTransactionManager",
)
@Configuration
class JpaRepositoryConfig
