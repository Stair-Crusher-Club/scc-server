package club.staircrusher.infra.persistence.sqldelight

import app.cash.sqldelight.Query
import app.cash.sqldelight.driver.jdbc.JdbcDriver
import java.sql.Connection
import javax.sql.DataSource

class SqlDelightJdbcDriver(
    private val dataSource: DataSource,
): JdbcDriver() {
    private val _isolationLevel = ThreadLocal<Int>()
    var isolationLevel: Int?
        get() = _isolationLevel.get()
        set(value) {
            value?.let { _isolationLevel.set(value) } ?: _isolationLevel.remove()
        }


    override fun Connection.endTransaction() {
        commit()
        autoCommit = true
        isolationLevel?.let { transactionIsolation = it }
        closeConnection(this)
    }

    override fun Connection.rollbackTransaction() {
        rollback()
        autoCommit = true
        isolationLevel?.let { transactionIsolation = it }
        closeConnection(this)
    }

    override fun Connection.beginTransaction() {
        check(autoCommit) {
            """
            Expected autoCommit to be true by default. For compatibility with SQLDelight make sure it is
            set to true when returning a connection from [JdbcDriver.getConnection()]
            """.trimIndent()
        }
        autoCommit = false
        isolationLevel?.let {
            val oldIsolationLevel = transactionIsolation
            transactionIsolation = it
            isolationLevel = oldIsolationLevel
        }
    }

    override fun getConnection(): Connection {
        return dataSource.connection
    }

    override fun closeConnection(connection: Connection) {
        connection.close()
    }

    override fun addListener(listener: Query.Listener, queryKeys: Array<String>) {
        // No-op. JDBC Driver is not set up for observing queries by default.
    }

    override fun removeListener(listener: Query.Listener, queryKeys: Array<String>) {
        // No-op. JDBC Driver is not set up for observing queries by default.
    }

    override fun notifyListeners(queryKeys: Array<String>) {
        // No-op. JDBC Driver is not set up for observing queries by default.
    }
}
