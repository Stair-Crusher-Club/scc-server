package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityUpvoteRepository
import org.springframework.stereotype.Component

@Component
class NoopBuildingAccessibilityUpvoteRepository : BuildingAccessibilityUpvoteRepository {
    override fun findByUserAndBuildingAccessibilityAndNotDeleted(
        userId: String,
        buildingAccessibility: BuildingAccessibility
    ): BuildingAccessibilityUpvote? {
        return  null
    }

    override fun getTotalUpvoteCount(buildingId: String): Int {
        return 0
    }

    override fun save(entity: BuildingAccessibilityUpvote): BuildingAccessibilityUpvote {
        return entity
    }

    override fun saveAll(entity: Collection<BuildingAccessibilityUpvote>): BuildingAccessibilityUpvote {
        return entity.first()
    }

    override fun removeAll() {
        // No-op
    }

    override fun findById(id: String): BuildingAccessibilityUpvote {
        throw IllegalArgumentException("BuildingAccessibilityUpvote of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): BuildingAccessibilityUpvote? {
        return null
    }
}
