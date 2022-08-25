package club.staircrusher.accessibility.domain.service

import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityCommentRepository
import club.staircrusher.stdlib.domain.DomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import java.time.Clock

class BuildingAccessibilityCommentService(
    private val clock: Clock,
    private val buildingAccessibilityCommentRepository: BuildingAccessibilityCommentRepository,
) {
    data class CreateParams(
        val buildingId: String,
        val userId: String?,
        val comment: String,
    )

    fun create(params: CreateParams): BuildingAccessibilityComment {
        val normalizedComment = params.comment.trim()
        if (normalizedComment.isBlank()) {
            throw DomainException("한 글자 이상의 의견을 제출해주세요.")
        }
        return buildingAccessibilityCommentRepository.add(BuildingAccessibilityComment(
            id = EntityIdGenerator.generateRandom(),
            buildingId = params.buildingId,
            userId = params.userId,
            comment = normalizedComment,
            createdAt = clock.instant(),
        ))
    }
}
