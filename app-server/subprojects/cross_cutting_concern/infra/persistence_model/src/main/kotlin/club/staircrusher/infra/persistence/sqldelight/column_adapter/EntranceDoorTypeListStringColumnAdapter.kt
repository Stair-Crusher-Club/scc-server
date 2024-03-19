package club.staircrusher.infra.persistence.sqldelight.column_adapter

import app.cash.sqldelight.ColumnAdapter
import club.staircrusher.accessibility.domain.model.EntranceDoorType

object EntranceDoorTypeListStringColumnAdapter : ColumnAdapter<List<EntranceDoorType>, String> {
    override fun decode(databaseValue: String): List<EntranceDoorType> {
        return databaseValue.split(delimiter).filter { it.isNotBlank() }.map { EntranceDoorType.valueOf(it) }
    }

    override fun encode(value: List<EntranceDoorType>): String = value.joinToString(delimiter)

    private const val delimiter = ",,"
}
