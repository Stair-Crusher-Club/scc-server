package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import org.hibernate.query.spi.Limit
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
}
