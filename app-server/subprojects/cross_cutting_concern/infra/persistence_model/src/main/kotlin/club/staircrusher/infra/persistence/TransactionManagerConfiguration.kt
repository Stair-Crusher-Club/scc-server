package club.staircrusher.infra.persistence

import club.staircrusher.stdlib.persistence.TransactionManager
import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager

@Configuration
class TransactionManagerConfiguration {
    @Bean
    fun sccJpaTransactionManager(
        entityManagerFactory: EntityManagerFactory,
    ): TransactionManager {
        return SccJpaTransactionManager(JpaTransactionManager(entityManagerFactory))
   }
}
