package club.staircrusher.stdlib.persistence

interface TransactionManager {
    fun <T> doInTransaction(block: () -> T): T
    fun <T> doInTransaction(isolationLevel: TransactionIsolationLevel, block: () -> T): T

    /**
     * 테스트용 메소드.
     */
    fun doAndRollback(block: () -> Any)
}
