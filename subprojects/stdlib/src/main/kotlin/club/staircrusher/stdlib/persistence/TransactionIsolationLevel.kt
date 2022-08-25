package club.staircrusher.stdlib.persistence

import java.sql.Connection

enum class TransactionIsolationLevel {
    REPEATABLE_READ {
        override fun toConnectionIsolationLevel() = Connection.TRANSACTION_REPEATABLE_READ
    },
    SERIALIZABLE {
        override fun toConnectionIsolationLevel() = Connection.TRANSACTION_SERIALIZABLE
    },
    ;

    abstract fun toConnectionIsolationLevel(): Int
}
