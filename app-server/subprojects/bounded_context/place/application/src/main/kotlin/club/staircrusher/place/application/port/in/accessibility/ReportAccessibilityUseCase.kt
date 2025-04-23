package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.port.out.accessibility.SlackService
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityReportRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityReport
import club.staircrusher.place.domain.model.accessibility.AccessibilityReportReason
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import org.springframework.beans.factory.annotation.Value

@Component
class ReportAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val slackService: SlackService,
    private val userProfileRepository: UserProfileRepository,
    private val placeApplicationService: PlaceApplicationService,
    private val accessibilityApplicationService: AccessibilityApplicationService,
    private val accessibilityReportRepository: AccessibilityReportRepository,
    @Value("\${scc.slack.channel.reportAccessibility:#scc-accessibility-report}") val accessibilityReportChannel: String,
) {
    fun handle(placeId: String, userId: String, reason: AccessibilityReportReason, detail: String? = null) {
        val (place, placeAccessibility, userProfile) = transactionManager.doInTransaction {
            val place = placeApplicationService.findPlace(placeId)
            val placeAccessibility =
                accessibilityApplicationService.doGetAccessibility(placeId, null).placeAccessibility
            val userProfile = userProfileRepository.findFirstByUserId(userId)

            // Create and save the accessibility report
            val report = AccessibilityReport.create(
                id = EntityIdGenerator.generateRandom(),
                placeId = placeId,
                userId = userId,
                reason = reason,
                detail = detail,
            )
            accessibilityReportRepository.save(report)

            return@doInTransaction Triple(place, placeAccessibility, userProfile)
        }

        val content = """
            접근성 정보에 대한 신고가 접수되었습니다.
            - 접근성 정보 Id: ${placeAccessibility?.value?.id}
            - 장소명: ${place?.name}
            - 주소: ${place?.address}
            - 신고 사유: $reason
            - 상세 내용: ${detail ?: "상세 내용 없음"}
            - 신고자: ${userProfile?.nickname ?: "익명"}
        """.trimIndent()

        slackService.send(
            accessibilityReportChannel,
            content
        )
    }
}
