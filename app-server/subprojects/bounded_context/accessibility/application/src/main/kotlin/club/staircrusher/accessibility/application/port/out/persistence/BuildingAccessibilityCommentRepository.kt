package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import org.springframework.data.repository.CrudRepository


interface BuildingAccessibilityCommentRepository : CrudRepository<BuildingAccessibilityComment, String> {
    fun findByBuildingId(buildingId: String): List<BuildingAccessibilityComment>
    fun removeByBuildingId(buildingId: String)

    data class CreateParams(
        val buildingId: String,
        val userId: String?,
        val comment: String,
    )
}
