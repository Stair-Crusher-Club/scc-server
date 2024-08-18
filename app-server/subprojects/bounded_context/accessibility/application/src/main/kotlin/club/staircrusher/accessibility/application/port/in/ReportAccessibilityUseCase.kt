package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.SlackService
import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.beans.factory.annotation.Value

@Component
class ReportAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val slackService: SlackService,
    private val placeApplicationService: PlaceApplicationService,
    @Value("\${scc.slack.channel.reportAccessibility:#scc-accessibility-report}") val accessibilityReportChannel: String,
) {
    fun handle(placeId: String, userId: String, reason: String?) {
        val place = transactionManager.doInTransaction {
            placeApplicationService.findPlace(placeId)
        }

        val content = """
            접근성 정보에 대한 신고가 접수되었습니다.
            |신고자: $userId
            |장소명: ${place?.name}
            |주소: ${place?.address}
            |신고 사유: ${reason ?: "사유 없음"}
        """.trimIndent()

        slackService.send(
            accessibilityReportChannel,
            content
        )
    }
}
