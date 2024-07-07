package club.staircrusher.infra.persistence.sqldelight.column_adapter

import app.cash.sqldelight.ColumnAdapter
import club.staircrusher.quest.domain.model.ClubQuestPurposeType

object ClubQuestPurposeTypeStringColumnAdapter : ColumnAdapter<ClubQuestPurposeType, String> {
    override fun decode(databaseValue: String): ClubQuestPurposeType {
        return ClubQuestPurposeType.valueOf(databaseValue)
    }

    override fun encode(value: ClubQuestPurposeType): String = value.name
}
