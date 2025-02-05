package club.staircrusher.infra.persistence.jpa.kotlin

import jakarta.persistence.EntityManagerFactory
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.TransactionException
import org.springframework.transaction.UnexpectedRollbackException
import org.springframework.transaction.support.DefaultTransactionStatus

/**
 * commit 도중에 [Throwable]이 발생하는 경우에도 트랜잭션을 롤백할 수 있도록 한다
 * commit 도중에 [Throwable]이 던져지는 경우의 예는 [jakarta.persistence.AttributeConverter]에서
 * 익셉션이 발생하는 경우가 있다.
 *
 * Spring 6.2 부터는 @EnableTransactionManagement(rollbackOn=ALL_EXCEPTIONS) 를 사용할 수 있지만,
 * 현재 6.0.7 버전을 사용하고 있으므로 일단 이렇게 처리한다
 * https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
 *
 */
class ThrowableAwareJpaTransactionManager(emf: EntityManagerFactory) : JpaTransactionManager(emf) {
    @Suppress("TooGenericExceptionThrown")
    override fun doCommit(status: DefaultTransactionStatus) {
        try {
            super.doCommit(status)
        } catch (e: Throwable) {
            when (e) {
                is UnexpectedRollbackException,
                is TransactionException,
                is RuntimeException,
                is Error,
                    ->
                    throw e
                else ->
                    throw RuntimeException(e)
            }
        }
    }
}
