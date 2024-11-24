package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.persistence.TransactionPropagation
import org.springframework.context.annotation.Primary

@Primary
@Component
class NoopTransactionManager : TransactionManager {
    override fun <T> doInTransaction(
        isolationLevel: TransactionIsolationLevel,
        propagation: TransactionPropagation,
        isReadOnly: Boolean,
        block: () -> T
    ): T {
        return block()
    }

    override fun doAfterCommit(block: () -> Unit) {
        block()
    }
}
