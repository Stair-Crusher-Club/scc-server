package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.domain.model.ClubQuestSummary
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager
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
        val cursor = cursorValue?.let { Cursor.parse(it) } ?: Cursor.INITIAL()
        val normalizedLimit = limit ?: DEFAULT_LIMIT

        val summaries = clubQuestRepository.findCursoredSummariesOrderByCreatedAtDesc(
            cursorCreatedAt = cursor.createdAt,
            cursorId = cursor.id,
            limit = normalizedLimit + 1, // 다음 페이지가 존재하는지 확인하기 위해 한 개를 더 조회한다.
        )

        val nextCursor = if (summaries.size > normalizedLimit) {
            Cursor(summaries[normalizedLimit - 1])
        } else {
            null
        }

        Result(
            list = summaries,
            nextCursor = nextCursor?.value,
        )
    }

    private data class Cursor(
        val id: String,
        val createdAt: Instant,
    ) {
        val value: String = "$id$DELIMITER${createdAt.toEpochMilli()}"

        constructor(summary: ClubQuestSummary) : this(
            id = summary.id,
            createdAt = summary.createdAt,
        )

        companion object {
            private const val DELIMITER = "__"

            fun parse(cursorValue: String): Cursor {
                return try {
                    val (id, createdAtMillis) = cursorValue.split(DELIMITER)
                    Cursor(id = id, createdAt = Instant.ofEpochMilli(createdAtMillis.toLong()))
                } catch (t: Throwable) {
                    throw SccDomainException("Invalid cursor value: $cursorValue", cause = t)
                }
            }

            fun INITIAL() = Cursor(id = "", createdAt = SccClock.instant())
        }
    }

    companion object {
        private const val DEFAULT_LIMIT = 50
    }
}
