package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.domain.model.ClubQuestSummary
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TimestampCursor
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant

@Component
class GetCursoredClubQuestSummariesUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
) {
    data class Result(
        val list: List<ClubQuestSummary>,
        val nextCursor: String?,
    )

    fun handle(
        limit: Int?,
        cursorValue: String?,
    ): Result = transactionManager.doInTransaction {
        val cursor = cursorValue?.let { Cursor.parse(it) } ?: Cursor.initial()
        val normalizedLimit = limit ?: DEFAULT_LIMIT

        val pageRequest = PageRequest.of(
            0,
            normalizedLimit,
            Sort.by(
                listOf(
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id"),
                ),
            ),
        )
        val result = clubQuestRepository.findCursoredSummaries(
            cursorCreatedAt = cursor.timestamp,
            cursorId = cursor.id,
            pageable = pageRequest,
        )

        val nextCursor = if (result.hasNext()) {
            Cursor(result.content[normalizedLimit - 1])
        } else {
            null
        }

        Result(
            list = result.content,
            nextCursor = nextCursor?.value,
        )
    }

    private data class Cursor(
        val questId: String,
        val createdAt: Instant,
    ) : TimestampCursor(questId, createdAt) {
        constructor(summary: ClubQuestSummary) : this(
            questId = summary.id,
            createdAt = summary.createdAt,
        )

        companion object {
            fun parse(cursorValue: String) = TimestampCursor.parse(cursorValue)

            fun initial() = TimestampCursor.initial()
        }
    }

    companion object {
        private const val DEFAULT_LIMIT = 50
    }
}
