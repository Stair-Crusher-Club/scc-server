package club.staircrusher.infra.persistence

import club.staircrusher.infra.persistence.jpa.SccPlatformTransactionManager
import club.staircrusher.infra.persistence.jpa.kotlin.ThrowableAwareJpaTransactionManager
import club.staircrusher.stdlib.persistence.TransactionManager
import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.TransactionManagementConfigurer

@Configuration
@EnableTransactionManagement
class TransactionManagerConfiguration(
    private val entityManagerFactory: EntityManagerFactory,
) : TransactionManagementConfigurer {

    @Bean
    fun sccJpaTransactionManager(): TransactionManager {
        return SccJpaTransactionManager(
            SccPlatformTransactionManager(ThrowableAwareJpaTransactionManager(entityManagerFactory))
        )
   }

    // `@Transactional` annotation 에서 SccPlatformTransactionManager 를 사용하도록
    override fun annotationDrivenTransactionManager(): org.springframework.transaction.TransactionManager {
        return SccPlatformTransactionManager(ThrowableAwareJpaTransactionManager(entityManagerFactory))
    }
}
