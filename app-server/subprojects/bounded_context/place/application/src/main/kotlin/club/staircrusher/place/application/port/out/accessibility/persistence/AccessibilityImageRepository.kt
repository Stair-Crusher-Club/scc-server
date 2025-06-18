package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface AccessibilityImageRepository : CrudRepository<AccessibilityImage, String> {
    fun findByAccessibilityIdAndAccessibilityType(accessibilityId: String, accessibilityType: AccessibilityImage.AccessibilityType): List<AccessibilityImage>

    @Query("""
        SELECT images
        FROM AccessibilityImage as images
        WHERE
            images.lastPostProcessedAt IS NULL
            OR
            images.lastPostProcessedAt < :at
        ORDER BY images.createdAt DESC
        LIMIT 10
    """)
    fun findBatchTargetsBefore(at: Instant): List<AccessibilityImage>

    @Query("""
        SELECT ai.*
        FROM accessibility_image ai
        WHERE
            (:inspectionResultType IS NULL OR
                (
                    (:inspectionResultType = 'Visible' AND ai.inspection_result LIKE '%"Visible"%') OR
                    (:inspectionResultType = 'NotVisible' AND ai.inspection_result LIKE '%"NotVisible"%')
                )
            )
            AND (
                (ai.created_at = :cursorCreatedAt AND ai.id < :cursorId)
                OR (ai.created_at < :cursorCreatedAt)
            )
        ORDER BY ai.created_at DESC, ai.id DESC
        LIMIT :limit
    """, nativeQuery = true)
    fun searchForAdmin(
        inspectionResultType: String?,
        cursorCreatedAt: Instant,
        cursorId: String,
        limit: Int,
    ): List<AccessibilityImage>
}
