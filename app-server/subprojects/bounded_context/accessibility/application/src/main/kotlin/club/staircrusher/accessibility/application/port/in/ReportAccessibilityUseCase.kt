package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.SlackService
import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.beans.factory.annotation.Value

@Component
class ReportAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val slackService: SlackService,
    private val placeApplicationService: PlaceApplicationService,
    private val accessibilityApplicationService: AccessibilityApplicationService,
    @Value("\${scc.slack.channel.reportAccessibility:#scc-accessibility-report}") val accessibilityReportChannel: String,
) {
    fun handle(placeId: String, authUser: AuthUser, reason: String?) {
        val (place, placeAccessibility) = transactionManager.doInTransaction {
            val place = placeApplicationService.findPlace(placeId)
            val placeAccessibility = accessibilityApplicationService.doGetAccessibility(placeId, null).placeAccessibility

            return@doInTransaction place to placeAccessibility
        }

        val content = """
            접근성 정보에 대한 신고가 접수되었습니다.
            - 접근성 정보 Id: ${placeAccessibility?.value?.id}
            - 장소명: ${place?.name}
            - 주소: ${place?.address}
            - 신고 사유: ${reason ?: "사유 없음"}
            - 신고자: ${authUser.nickname}
        """.trimIndent()

        slackService.send(
            accessibilityReportChannel,
            content
        )
    }
}
