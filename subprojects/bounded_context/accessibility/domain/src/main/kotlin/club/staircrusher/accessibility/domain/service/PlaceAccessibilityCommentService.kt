package club.staircrusher.accessibility.domain.service

import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.repository.PlaceAccessibilityCommentRepository
import club.staircrusher.stdlib.domain.DomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.di.annotation.Component
import java.time.Clock

@Component
class PlaceAccessibilityCommentService(
    private val clock: Clock,
    private val placeAccessibilityCommentRepository: PlaceAccessibilityCommentRepository,
) {
    data class CreateParams(
        val placeId: String,
        val userId: String?,
        val comment: String,
    )

    fun create(params: CreateParams): PlaceAccessibilityComment {
        val normalizedComment = params.comment.trim()
        if (normalizedComment.isBlank()) {
            throw DomainException("한 글자 이상의 의견을 제출해주세요.")
        }
        return placeAccessibilityCommentRepository.save(
            PlaceAccessibilityComment(
            id = EntityIdGenerator.generateRandom(),
            placeId = params.placeId,
            userId = params.userId,
            comment = normalizedComment,
            createdAt = clock.instant(),
        )
        )
    }
}
