package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.slack.application.port.out.web.SlackService
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityUpvoteRepository
import club.staircrusher.place.application.port.out.place.persistence.PlaceRepository
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibilityUpvote
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull

@Component
class GivePlaceAccessibilityUpvoteUseCase(
    private val transactionManager: TransactionManager,
    private val slackService: SlackService,
    private val placeRepository: PlaceRepository,
    private val userProfileRepository: UserProfileRepository,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeAccessibilityUpvoteRepository: PlaceAccessibilityUpvoteRepository,
    @Value("\${scc.slack.channel.reportAccessibility:#scc-accessibility-report}") val accessibilityReportChannel: String,
) {
    fun handle(
        userId: String,
        placeAccessibilityId: String,
    ) = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val placeAccessibility = placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId)
            ?: throw IllegalArgumentException("PlaceAccessibility of id $placeAccessibilityId does not exist.")
        val existingUpvote = placeAccessibilityUpvoteRepository.findExistingUpvote(userId, placeAccessibilityId)
        existingUpvote ?: placeAccessibilityUpvoteRepository.save(
            PlaceAccessibilityUpvote(
                id = EntityIdGenerator.generateRandom(),
                userId = userId,
                placeAccessibilityId = placeAccessibilityId,
                createdAt = SccClock.instant(),
            )
        )

        val place = placeRepository.findByIdOrNull(placeAccessibility.placeId)
        val userProfile = userProfileRepository.findFirstByUserId(userId)
        val content = """
            접근성 정보가 도움이 돼요
            - 접근성 정보 Id: ${placeAccessibility.id}
            - 장소명: ${place?.name}
            - 주소: ${place?.address}
            - 신고자: ${userProfile?.nickname ?: "익명"}
        """.trimIndent()

        transactionManager.doAfterCommit {
            slackService.send(
                accessibilityReportChannel,
                content,
            )
        }
    }
}
