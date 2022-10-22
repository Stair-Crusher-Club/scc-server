package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.Transaction
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class NoopTransactionManager : TransactionManager {
    override fun <T> doInTransaction(block: Transaction<T>.() -> T): T {
        return NoopTransaction<T>().block()
    }

    override fun <T> doInTransaction(isolationLevel: TransactionIsolationLevel, block: Transaction<T>.() -> T): T {
        return NoopTransaction<T>().block()
    }

    private class NoopTransaction<T> : Transaction<T> {
        override fun afterCommit(block: () -> Unit) {
            TODO("Not yet implemented")
        }

        override fun afterRollback(block: () -> Unit) {
            TODO("Not yet implemented")
        }

        override fun rollback(returnValue: T) {
            TODO("Not yet implemented")
        }

    }
}
