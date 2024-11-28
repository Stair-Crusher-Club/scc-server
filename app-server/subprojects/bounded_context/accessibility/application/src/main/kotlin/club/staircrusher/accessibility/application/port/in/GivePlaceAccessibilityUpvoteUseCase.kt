package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.SlackService
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityUpvoteRepository
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityUpvote
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import java.time.Clock

@Component
class GivePlaceAccessibilityUpvoteUseCase(
    private val transactionManager: TransactionManager,
    private val slackService: SlackService,
    private val placeRepository: PlaceRepository,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeAccessibilityUpvoteRepository: PlaceAccessibilityUpvoteRepository,
    @Value("\${scc.slack.channel.reportAccessibility:#scc-accessibility-report}") val accessibilityReportChannel: String,
    private val clock: Clock,
) {
    private val logger = KotlinLogging.logger {}

    fun handle(
        user: AuthUser,
        placeAccessibilityId: String,
    ) = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val placeAccessibility = placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId)
            ?: throw IllegalArgumentException("PlaceAccessibility of id $placeAccessibilityId does not exist.")
        val existingUpvote = placeAccessibilityUpvoteRepository.findExistingUpvote(user.id, placeAccessibilityId)
        existingUpvote ?: placeAccessibilityUpvoteRepository.save(
            PlaceAccessibilityUpvote(
                id = EntityIdGenerator.generateRandom(),
                userId = user.id,
                placeAccessibilityId = placeAccessibilityId,
                createdAt = clock.instant(),
            )
        )

        val place = placeRepository.findByIdOrNull(placeAccessibility.placeId)
        val content = """
            접근성 정보가 도움이 돼요
            - 접근성 정보 Id: ${placeAccessibility.id}
            - 장소명: ${place?.name}
            - 주소: ${place?.address}
            - 신고자: ${user.nickname}
        """.trimIndent()
        logger.info("Content that will be sent to slack: $content")

        transactionManager.doAfterCommit {
            logger.info("Give place accessibility upvote after commit for $placeAccessibilityId")
            slackService.send(
                accessibilityReportChannel,
                content,
            )
        }
    }
}
