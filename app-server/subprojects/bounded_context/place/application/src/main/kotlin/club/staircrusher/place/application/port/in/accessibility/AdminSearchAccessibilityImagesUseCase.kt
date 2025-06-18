package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TimestampCursor
import java.time.Instant

@Component
class AdminSearchAccessibilityImagesUseCase(
    private val accessibilityImageRepository: AccessibilityImageRepository,
) {
    data class Result(
        val items: List<AccessibilityImage>,
        val cursor: String?,
    )

    fun handle(
        inspectionResultType: String?,
        cursorValue: String?,
        limit: Int?,
    ): Result {
        val cursor = cursorValue?.let { Cursor.parse(it) } ?: Cursor.initial()
        val normalizedLimit = limit ?: DEFAULT_LIMIT

        val accessibilityImages = accessibilityImageRepository.searchForAdmin(
            inspectionResultType = inspectionResultType,
            cursorCreatedAt = cursor.timestamp,
            cursorId = cursor.id,
            limit = normalizedLimit + 1, // 다음 페이지가 존재하는지 확인하기 위해 한 개를 더 조회한다.
        )

        val nextCursorValue = if (accessibilityImages.size > normalizedLimit) {
            Cursor(accessibilityImages[normalizedLimit - 1]).value
        } else {
            null
        }

        return Result(
            items = accessibilityImages.take(normalizedLimit),
            cursor = nextCursorValue,
        )
    }

    private data class Cursor(
        val createdAt: Instant,
        val accessibilityImageId: String,
    ) : TimestampCursor(createdAt, accessibilityImageId) {
        constructor(accessibilityImage: AccessibilityImage) : this(
            createdAt = accessibilityImage.createdAt,
            accessibilityImageId = accessibilityImage.id,
        )

        companion object {
            fun parse(cursorValue: String) = TimestampCursor.parse(cursorValue)

            fun initial() = TimestampCursor.initial()
        }
    }

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}
