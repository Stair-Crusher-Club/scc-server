package club.staircrusher.infra.persistence.sqldelight.column_adapter

import app.cash.sqldelight.ColumnAdapter
import club.staircrusher.user.domain.model.UserMobilityTool

object UserMobilityToolStringColumnAdapter : ColumnAdapter<List<UserMobilityTool>, String> {
    override fun decode(databaseValue: String): List<UserMobilityTool> {
        return databaseValue.split(delimiter).filter { it.isNotBlank() }.map { UserMobilityTool.valueOf(it) }
    }

    override fun encode(value: List<UserMobilityTool>): String = value.joinToString(delimiter)

    private const val delimiter = ",,"
}
