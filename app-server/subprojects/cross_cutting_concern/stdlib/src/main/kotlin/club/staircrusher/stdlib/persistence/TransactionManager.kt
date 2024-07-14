package club.staircrusher.stdlib.persistence

interface TransactionManager {
    fun <T> doInTransaction(block: () -> T): T
    fun <T> doInTransaction(isolationLevel: TransactionIsolationLevel, block: () -> T): T

    fun doAfterCommit(block: () -> Unit)
}
