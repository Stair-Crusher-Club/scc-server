package club.staircrusher.stdlib.persistence

enum class TransactionIsolationLevel {
    DEFAULT,
    READ_COMMITTED,
    REPEATABLE_READ,
    SERIALIZABLE,
    ;
}
