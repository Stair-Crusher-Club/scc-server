package club.staircrusher.infra.persistence.sqldelight

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.Transaction
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class TransactionManager(
    private val db: DB
): TransactionManager {
    override fun <T> doInTransaction(block: Transaction<T>.() -> T): T {
        return db.scc.transactionWithResult {
            Transaction(this).block()
        }
    }

    override fun <T> doInTransaction(isolationLevel: TransactionIsolationLevel, block: Transaction<T>.() -> T): T {
        return db.scc.transactionWithResult {
            Transaction(this).block()
        }
    }
}
