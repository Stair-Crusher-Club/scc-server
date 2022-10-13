package club.staircrusher.infra.persistence.sqldelight

import app.cash.sqldelight.TransactionWithReturn
import club.staircrusher.stdlib.persistence.Transaction

class Transaction<T>(
    private val transaction: TransactionWithReturn<T>
) : Transaction<T> {
    override fun afterCommit(block: () -> Unit) {
        transaction.afterCommit(block)
    }

    override fun afterRollback(block: () -> Unit) {
        transaction.afterRollback(block)
    }

    override fun rollback(returnValue: T) {
        transaction.rollback(returnValue)
    }
}
