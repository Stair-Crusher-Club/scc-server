package club.staircrusher.infra.persistence

import club.staircrusher.infra.persistence.jpa.SccPlatformTransactionManager
import club.staircrusher.infra.persistence.jpa.kotlin.ThrowableAwareJpaTransactionManager
import club.staircrusher.stdlib.persistence.TransactionManager
import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class TransactionManagerConfiguration {
    @Bean("transactionManager")
    @Primary
    fun sccPlatformTransactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        return SccPlatformTransactionManager(ThrowableAwareJpaTransactionManager(entityManagerFactory))
    }

    @Bean
    fun sccJpaTransactionManager(platformTransactionManager: PlatformTransactionManager): TransactionManager {
        return SccJpaTransactionManager(platformTransactionManager)
   }
}
