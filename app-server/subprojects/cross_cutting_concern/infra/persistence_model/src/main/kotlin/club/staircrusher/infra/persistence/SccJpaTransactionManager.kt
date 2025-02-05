package club.staircrusher.infra.persistence

import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.persistence.TransactionPropagation
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate

class SccJpaTransactionManager( // Spring이 제공하는 JpaTransactionManager bean과 이름이 겹치지 않도록 한다.
    private val platformTransactionManager: PlatformTransactionManager,
) : TransactionManager {
    private val transactionTemplates: Map<TransactionPreset, TransactionTemplate> = run {
        TransactionIsolationLevel.values().flatMap { isolationLevel ->
            TransactionPropagation.values().flatMap { propagation ->
                listOf(true, false).map { isReadOnly ->
                    val txTemplate = TransactionTemplate(platformTransactionManager).also {
                        it.isolationLevel = isolationLevel.toSpring()
                        it.propagationBehavior = propagation.toSpring()
                        it.isReadOnly = isReadOnly
                    }

                    TransactionPreset(isolationLevel, propagation, isReadOnly) to txTemplate
                }
            }
        }.toMap()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> doInTransaction(
        isolationLevel: TransactionIsolationLevel,
        propagation: TransactionPropagation,
        isReadOnly: Boolean,
        block: () -> T
    ): T {
        val transactionTemplate = transactionTemplates[TransactionPreset(isolationLevel, propagation, isReadOnly)]
            ?: TransactionTemplate(platformTransactionManager, DefaultTransactionDefinition())

        return transactionTemplate.execute {
            block()
        } as T
    }

    override fun doAfterCommit(block: () -> Unit) {
        if (SccTxStateHolder.get() != SccTxStateHolder.State.ACTIVE) {
            // Transactional 하지 않은 요청에 대해서는 바로 처리해버린다.
            block()
            return
        }
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    val parent = SccTxStateHolder.get()
                    SccTxStateHolder.set(SccTxStateHolder.State.IN_AFTER_COMMIT)
                    try {
                        block()
                    } finally {
                        SccTxStateHolder.set(parent)
                    }
                }
            },
        )
    }

    private fun TransactionIsolationLevel.toSpring() = when (this) {
        TransactionIsolationLevel.DEFAULT -> TransactionDefinition.ISOLATION_DEFAULT
        TransactionIsolationLevel.READ_COMMITTED -> TransactionDefinition.ISOLATION_READ_COMMITTED
        TransactionIsolationLevel.REPEATABLE_READ -> TransactionDefinition.ISOLATION_REPEATABLE_READ
        TransactionIsolationLevel.SERIALIZABLE -> TransactionDefinition.ISOLATION_SERIALIZABLE
    }

    private fun TransactionPropagation.toSpring() = when (this) {
        TransactionPropagation.REQUIRED -> TransactionDefinition.PROPAGATION_REQUIRED
        TransactionPropagation.REQUIRES_NEW -> TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    data class TransactionPreset(
        val isolationLevel: TransactionIsolationLevel,
        val propagationLevel: TransactionPropagation,
        val isReadOnly: Boolean,
    )
}
