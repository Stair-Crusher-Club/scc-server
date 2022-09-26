package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityCommentRepository
import org.springframework.stereotype.Component

@Component
class NoopBuildingAccessibilityCommentRepository : BuildingAccessibilityCommentRepository {
    override fun findByBuildingId(buildingId: String): List<BuildingAccessibilityComment> {
        return emptyList()
    }

    override fun save(entity: BuildingAccessibilityComment): BuildingAccessibilityComment {
        return entity
    }

    override fun saveAll(entity: Collection<BuildingAccessibilityComment>): BuildingAccessibilityComment {
        return entity.first()
    }

    override fun removeAll() {
        // No-op
    }

    override fun findById(id: String): BuildingAccessibilityComment {
        throw IllegalArgumentException("BuildingAccessibilityComment of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): BuildingAccessibilityComment? {
        return null
    }
}
