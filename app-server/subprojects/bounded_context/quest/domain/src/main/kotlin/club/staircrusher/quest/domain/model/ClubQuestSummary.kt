package club.staircrusher.quest.domain.model

import java.time.Instant

data class ClubQuestSummary(
    val id: String,
    val name: String,
    val purposeType: ClubQuestPurposeType,
    val startAt: Instant,
    val endAt: Instant,
    val shortenedUrl: String?,
    val createdAt: Instant,
)
