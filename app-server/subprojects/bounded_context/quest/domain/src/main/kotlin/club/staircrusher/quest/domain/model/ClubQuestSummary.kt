package club.staircrusher.quest.domain.model

import java.time.Instant

interface ClubQuestSummary {
    val id: String
    val name: String
    val purposeType: ClubQuestPurposeType
    val startAt: Instant
    val endAt: Instant
    val shortenedAdminUrl: String?
    val createdAt: Instant
}
