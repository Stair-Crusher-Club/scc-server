package club.staircrusher.stdlib.persistence

interface Transaction<T> {
    fun afterCommit(block: () -> Unit)
    fun afterRollback(block: () -> Unit)
    fun rollback(returnValue: T)
}
