package club.staircrusher.infra.persistence

import club.staircrusher.stdlib.persistence.TransactionManager
import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class TransactionManagerConfiguration {
    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        return SccPlatformTransactionManager(KotlinJpaTransactionManager(entityManagerFactory))
    }

    @Bean
    fun sccJpaTransactionManager(platformTransactionManager: PlatformTransactionManager): TransactionManager {
        return SccJpaTransactionManager(platformTransactionManager)
   }
}
