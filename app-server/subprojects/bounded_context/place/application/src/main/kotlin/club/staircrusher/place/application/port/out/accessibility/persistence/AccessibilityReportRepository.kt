package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.AccessibilityReport
import org.springframework.data.repository.CrudRepository

interface AccessibilityReportRepository : CrudRepository<AccessibilityReport, String> {
    fun findByPlaceId(placeId: String): List<AccessibilityReport>
    fun findByUserId(userId: String): List<AccessibilityReport>
}
