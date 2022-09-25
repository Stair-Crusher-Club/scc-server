package club.staircrusher.stdlib.persistence

import org.springframework.stereotype.Component

@Component
class NoopTransactionManager : TransactionManager {
    override fun <T> doInTransaction(block: () -> T): T {
        return block()
    }

    override fun <T> doInTransaction(isolationLevel: TransactionIsolationLevel, block: () -> T): T {
        return block()
    }

    override fun doAndRollback(block: () -> Any) {
        block()
    }
}
