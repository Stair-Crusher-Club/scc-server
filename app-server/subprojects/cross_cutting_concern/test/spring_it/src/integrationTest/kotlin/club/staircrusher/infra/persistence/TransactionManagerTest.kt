package club.staircrusher.infra.persistence

import club.staircrusher.testing.spring_it.base.SccSpringITBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class TransactionManagerTest : SccSpringITBase() {

    @BeforeEach
    fun setUp() {
        SccJpaTransactionManager.currentTxState.remove()
    }

    @AfterEach
    fun tearDown() {
        // make sure cleanup
        Assertions.assertEquals(SccJpaTransactionManager.TxState.NONE, SccJpaTransactionManager.currentTxState.get())
    }

    @Test
    fun afterCommitInTransaction() {
        val callCount = AtomicInteger(0)
        transactionManager.doInTransaction {
            transactionManager.doAfterCommit {
                callCount.incrementAndGet()
            }
            Assertions.assertEquals(0, callCount.get())
        }
        Assertions.assertEquals(1, callCount.get())
    }

    @Test
    fun afterCommitInTransactionShouldNotBeCalledOnRollback() {
        Assertions.assertThrows(ExpectedException::class.java) {
            transactionManager.doInTransaction {
                transactionManager.doAfterCommit {
                    Assertions.fail("")
                }
                throw ExpectedException()
            }
        }
    }

    @Test
    fun exceptionThrownWhileAfterCommitShouldBePropagated() {
        Assertions.assertThrows(ExpectedException::class.java) {
            transactionManager.doInTransaction {
                transactionManager.doAfterCommit {
                    throw ExpectedException()
                }
            }
        }
    }

    @Test
    fun afterCommitInNestedTransaction() {
        val callCount = AtomicInteger(0)
        transactionManager.doInTransaction {
            transactionManager.doInTransaction {
                transactionManager.doAfterCommit {
                    callCount.incrementAndGet()
                }
            }
            Assertions.assertEquals(0, callCount.get())
        }
        Assertions.assertEquals(1, callCount.get())
    }

    @Test
    fun afterCommitWithoutTransaction() {
        val callCount = AtomicInteger(0)
        transactionManager.doAfterCommit {
            callCount.incrementAndGet()
        }
        Assertions.assertEquals(1, callCount.get())
    }

    @Test
    fun nestedAfterCommitInTransaction() {
        val callCount = AtomicInteger(0)
        transactionManager.doInTransaction {
            transactionManager.doAfterCommit {
                transactionManager.doAfterCommit {
                    callCount.incrementAndGet()
                }
            }
            Assertions.assertEquals(0, callCount.get())
        }
        Assertions.assertEquals(1, callCount.get())
    }

    @Test
    fun afterCommitInTransactionInAfterCommit() {
        val callCount = AtomicInteger(0)
        transactionManager.doInTransaction {
            transactionManager.doAfterCommit {
                transactionManager.doInTransaction {
                    transactionManager.doAfterCommit {
                        callCount.incrementAndGet()
                    }
                    Assertions.assertEquals(0, callCount.get())
                }
            }
            Assertions.assertEquals(0, callCount.get())
        }
        Assertions.assertEquals(1, callCount.get())
    }

    @Test
    fun nestedAfterCommitInTransactionInAfterCommit() {
        val callCount = AtomicInteger(0)
        transactionManager.doInTransaction {
            transactionManager.doAfterCommit {
                transactionManager.doInTransaction {
                    transactionManager.doAfterCommit {
                        transactionManager.doAfterCommit {
                            callCount.incrementAndGet()
                        }
                    }
                    Assertions.assertEquals(0, callCount.get())
                }
            }
            Assertions.assertEquals(0, callCount.get())
        }
        Assertions.assertEquals(1, callCount.get())
    }

    class ExpectedException : RuntimeException()
}
