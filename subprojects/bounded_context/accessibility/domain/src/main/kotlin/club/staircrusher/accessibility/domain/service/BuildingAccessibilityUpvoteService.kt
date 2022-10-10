package club.staircrusher.accessibility.domain.service

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityUpvoteRepository
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class BuildingAccessibilityUpvoteService(
    private val clock: Clock,
    private val buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository,
) {
    fun giveUpvote(user: AuthUser, buildingAccessibility: BuildingAccessibility): BuildingAccessibilityUpvote {
        val existingUpvote = buildingAccessibilityUpvoteRepository.findByUserAndBuildingAccessibilityAndNotDeleted(user.id, buildingAccessibility)
        if (existingUpvote != null) {
            return existingUpvote
        }

        return buildingAccessibilityUpvoteRepository.save(
            BuildingAccessibilityUpvote(
            id = EntityIdGenerator.generateRandom(),
            userId = user.id,
            buildingAccessibility = buildingAccessibility,
            createdAt = clock.instant()
        )
        )
    }

    fun cancelUpvote(user: AuthUser, buildingAccessibility: BuildingAccessibility) {
        buildingAccessibilityUpvoteRepository.findByUserAndBuildingAccessibilityAndNotDeleted(user.id, buildingAccessibility)?.let {
            it.deletedAt = clock.instant()
            buildingAccessibilityUpvoteRepository.save(it)
        }
    }

    fun isUpvoted(userId: String, buildingAccessibility: BuildingAccessibility): Boolean {
        return buildingAccessibilityUpvoteRepository.findByUserAndBuildingAccessibilityAndNotDeleted(userId, buildingAccessibility) != null
    }
}
