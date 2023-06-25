package club.staircrusher.stdlib.persistence

interface TransactionManager {
    fun <T> doInTransaction(block: Transaction<T>.() -> T): T
    fun <T> doInTransaction(isolationLevel: TransactionIsolationLevel, block: Transaction<T>.() -> T): T
}
