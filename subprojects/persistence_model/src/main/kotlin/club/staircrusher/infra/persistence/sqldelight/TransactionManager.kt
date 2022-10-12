package club.staircrusher.infra.persistence.sqldelight

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class TransactionManager(
    private val db: DB
): TransactionManager {
    override fun <T> doInTransaction(block: () -> T): T {
        return db.scc.transactionWithResult { block() }
    }

    override fun <T> doInTransaction(isolationLevel: TransactionIsolationLevel, block: () -> T): T {
        return db.scc.transactionWithResult { block() }
    }

    override fun doAndRollback(block: () -> Any) {
        return db.scc.transaction {
            block()
            rollback()
        }
    }
}
