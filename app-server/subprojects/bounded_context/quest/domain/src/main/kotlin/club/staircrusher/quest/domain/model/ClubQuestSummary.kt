package club.staircrusher.quest.domain.model

import java.time.Instant

data class ClubQuestSummary(
    val id: String,
    val name: String,
    val createdAt: Instant,
)
