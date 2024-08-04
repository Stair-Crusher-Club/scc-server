package club.staircrusher.infra.persistence

import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate

@Component
class SccJpaTransactionManager( // Spring이 제공하는 JpaTransactionManager bean과 이름이 겹치지 않도록 한다.
    private val transactionTemplate: TransactionTemplate,
) : TransactionManager {
    override fun <T> doInTransaction(block: () -> T): T {
        /**
         * 전체 엔티티에 대해 한 번에 jpa로 변환하는 게 아닌 이상, sqldelight와 jpa를 동시에 사용하는 기간이 반드시 발생한다.
         *
         * sqldelight는 어차피 ORM이 아니라 SQL을 kotlin API로 type-safe하게 쓰는 역할만 하므로,
         * JPA의 트랜잭션 라이프사이클에 의존해도 괜찮다.
         * 따라서 여기서는 JPA 트랜잭션을 사용한다.
         */
        return transactionTemplate.execute {
            block()
        } as T
    }

    override fun <T> doInTransaction(isolationLevel: TransactionIsolationLevel, block: () -> T): T {
        val transactionDefinition = DefaultTransactionDefinition().apply {
            this.isolationLevel = isolationLevel.toSpring()
        }

        return TransactionTemplate(transactionTemplate.transactionManager!!, transactionDefinition).execute {
            block()
        } as T
    }

    override fun doAfterCommit(block: () -> Unit) {
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                block()
            }
        })
    }

    private fun TransactionIsolationLevel.toSpring() = when (this) {
        TransactionIsolationLevel.READ_COMMITTED -> TransactionDefinition.ISOLATION_READ_COMMITTED
        TransactionIsolationLevel.REPEATABLE_READ -> TransactionDefinition.ISOLATION_REPEATABLE_READ
        TransactionIsolationLevel.SERIALIZABLE -> TransactionDefinition.ISOLATION_SERIALIZABLE
    }
}
