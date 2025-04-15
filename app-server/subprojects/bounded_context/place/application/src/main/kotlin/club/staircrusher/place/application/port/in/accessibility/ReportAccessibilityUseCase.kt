package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.port.out.accessibility.SlackService
import club.staircrusher.stdlib.di.annotation.Component
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
    @Value("\${scc.slack.channel.reportAccessibility:#scc-accessibility-report}") val accessibilityReportChannel: String,
) {
    fun handle(placeId: String, userId: String, reason: String?) {
        val (place, placeAccessibility, userProfile) = transactionManager.doInTransaction {
            val place = placeApplicationService.findPlace(placeId)
            val placeAccessibility = accessibilityApplicationService.doGetAccessibility(placeId, null).placeAccessibility
            val userProfile = userProfileRepository.findFirstByUserId(userId)

            return@doInTransaction Triple(place, placeAccessibility, userProfile)
        }

        val content = """
            접근성 정보에 대한 신고가 접수되었습니다.
            - 접근성 정보 Id: ${placeAccessibility?.value?.id}
            - 장소명: ${place?.name}
            - 주소: ${place?.address}
            - 신고 사유: ${reason ?: "사유 없음"}
            - 신고자: ${userProfile?.nickname ?: "익명"}
        """.trimIndent()

        slackService.send(
            accessibilityReportChannel,
            content
        )
    }
}
