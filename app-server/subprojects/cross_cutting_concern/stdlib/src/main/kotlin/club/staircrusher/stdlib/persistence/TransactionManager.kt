package club.staircrusher.stdlib.persistence

interface TransactionManager {
    fun <T> doInTransaction(
        isolationLevel: TransactionIsolationLevel = TransactionIsolationLevel.DEFAULT,
        propagation: TransactionPropagation = TransactionPropagation.REQUIRED,
        isReadOnly: Boolean = false,
        block: () -> T
    ): T

    fun doAfterCommit(block: () -> Unit)
}
