package club.staircrusher.infra.persistence.jpa

import club.staircrusher.infra.persistence.SccTxStateHolder
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

class SccPlatformTransactionManager(
    private val delegate: PlatformTransactionManager
) : PlatformTransactionManager {
    override fun getTransaction(definitionParam: TransactionDefinition?): TransactionStatus {
        val parent = SccTxStateHolder.get()
        if (parent == SccTxStateHolder.State.ACTIVE) {
            // 이미 활성화된 트랜잭션이 있으므로 delegate에 그대로 맡긴다.
            return delegate.getTransaction(definitionParam)
        }

        val definition = if (SccTxStateHolder.get() == SccTxStateHolder.State.IN_AFTER_COMMIT) {
            // afterCommit 안에서 트랜잭션이 생성되는 경우이다.
            // 반드시 새로운 트랜잭션이 생기도록 propagationBehavior 를 강제 지정한다.
            copyTransactionDefinition(definitionParam).apply {
                propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
            }
        } else {
            definitionParam
        }

        val status = delegate.getTransaction(definition)
        SccTxStateHolder.set(SccTxStateHolder.State.ACTIVE)
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCompletion(status: Int) {
                    SccTxStateHolder.set(parent)
                }
            },
        )

        return status
    }

    override fun commit(status: TransactionStatus) {
        delegate.commit(status)
    }

    override fun rollback(status: TransactionStatus) {
        delegate.rollback(status)
    }

    private fun copyTransactionDefinition(definition: TransactionDefinition?): DefaultTransactionDefinition {
        return if (definition != null) {
            DefaultTransactionDefinition(definition)
        } else {
            DefaultTransactionDefinition()
        }
    }
}
