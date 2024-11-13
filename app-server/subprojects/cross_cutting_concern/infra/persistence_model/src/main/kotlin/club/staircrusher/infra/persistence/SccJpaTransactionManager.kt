package club.staircrusher.infra.persistence

import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.persistence.TransactionPropagation
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate

class SccJpaTransactionManager( // Spring이 제공하는 JpaTransactionManager bean과 이름이 겹치지 않도록 한다.
    private val delegate: PlatformTransactionManager,
) : TransactionManager, PlatformTransactionManager by delegate {
    private val transactionTemplates: Map<TransactionPreset, TransactionTemplate> = run {
        TransactionIsolationLevel.values().flatMap { isolationLevel ->
            TransactionPropagation.values().flatMap { propagation ->
                listOf(true, false).map { isReadOnly ->
                    val txTemplate = TransactionTemplate(this).also {
                        it.isolationLevel = isolationLevel.toSpring()
                        it.propagationBehavior = propagation.toSpring()
                        it.isReadOnly = isReadOnly
                    }

                    TransactionPreset(isolationLevel, propagation, isReadOnly) to txTemplate
                }
            }
        }.toMap()
    }

    override fun getTransaction(definitionParam: TransactionDefinition?): TransactionStatus {
        val parent = currentTxState.get()
        if (parent == TxState.ACTIVE) {
            // 이미 활성화된 트랜잭션이 있으므로 delegate 에 그대로 맡긴다.
            return delegate.getTransaction(definitionParam)
        }
        val definition = if (parent == TxState.IN_AFTER_COMMIT) {
            // afterCommit 안에서 트랜잭션이 생성되는 경우이다.
            // 반드시 새로운 트랜잭션이 생기도록 propagationBehavior 를 강제 지정한다.
            copyTransactionDefinition(definitionParam).apply {
                propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
            }
        } else {
            definitionParam
        }
        return delegate.getTransaction(definition)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> doInTransaction(
        isolationLevel: TransactionIsolationLevel,
        propagation: TransactionPropagation,
        isReadOnly: Boolean,
        block: () -> T
    ): T {
        val transactionPreset = TransactionPreset(isolationLevel, propagation, isReadOnly)
        val transactionTemplate = transactionTemplates[transactionPreset]
            ?: TransactionTemplate(this, DefaultTransactionDefinition())

        return transactionTemplate.execute {
            val parent = currentTxState.get()
            if (parent != TxState.ACTIVE) {
                currentTxState.set(TxState.ACTIVE)
            }

            try {
                block()
            } finally {
                currentTxState.set(parent)
            }
        } as T
    }

    override fun doAfterCommit(block: () -> Unit) {
        if (currentTxState.get() != TxState.ACTIVE) {
            // Transactional 하지 않은 요청에 대해서는 바로 처리해버린다.
            block()
            return
        }
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    val parent = currentTxState.get()
                    currentTxState.set(TxState.IN_AFTER_COMMIT)
                    try {
                        block()
                    } finally {
                        currentTxState.set(parent)
                    }
                }
            },
        )
    }

    private fun copyTransactionDefinition(definition: TransactionDefinition?): DefaultTransactionDefinition {
        return if (definition != null) {
            DefaultTransactionDefinition(definition)
        } else {
            DefaultTransactionDefinition()
        }
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

    enum class TxState {
        NONE,
        ACTIVE,
        IN_AFTER_COMMIT,
    }

    companion object {
        val currentTxState = ThreadLocal.withInitial { TxState.NONE }!!
    }
}
