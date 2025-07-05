package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.BuildingAccessibilityComment
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
