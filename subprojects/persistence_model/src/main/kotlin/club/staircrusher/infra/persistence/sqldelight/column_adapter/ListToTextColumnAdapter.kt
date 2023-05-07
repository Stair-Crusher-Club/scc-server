package club.staircrusher.infra.persistence.sqldelight.column_adapter

import app.cash.sqldelight.ColumnAdapter

abstract class ListToTextColumnAdapter<T> : ColumnAdapter<List<T>, String> {
    override fun decode(databaseValue: String): List<T> {
        return databaseValue.split(delimiter).filter { it.isNotBlank() }.map(::convertElementFromTextColumn)
    }

    override fun encode(value: List<T>): String {
        return value.joinToString(delimiter) { convertElementToTextColumn(it) }
    }

    abstract fun convertElementToTextColumn(element: T): String
    abstract fun convertElementFromTextColumn(text: String): T

    companion object {
        private const val delimiter = ",,"
    }
}
