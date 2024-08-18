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
