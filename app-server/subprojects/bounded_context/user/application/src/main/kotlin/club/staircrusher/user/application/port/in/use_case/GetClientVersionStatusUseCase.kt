package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.version.SemanticVersion
import club.staircrusher.user.domain.model.ClientVersionStatus
import club.staircrusher.user.domain.service.ClientVersionService

@Component
class GetClientVersionStatusUseCase(
    private val clientVersionService: ClientVersionService
) {
    fun handle(clientVersion: String): ClientVersionStatus {
        val version = SemanticVersion.parse(clientVersion) ?: return ClientVersionStatus(
            status = ClientVersionStatus.Status.UPGRADE_NEEDED,
            message = "Invalid version format"
        )

        clientVersionService.getUpgradedNeededVersion().let { minVersion ->
            if (version < minVersion) return ClientVersionStatus(
                status = ClientVersionStatus.Status.UPGRADE_NEEDED,
                message = "안정적인 서비스 사용을 위해 최신 버전으로 업데이트 해주세요."
            )
        }

        clientVersionService.getUpgradedRecommendedVersion().let { minVersion ->
            if (version < minVersion) return ClientVersionStatus(
                status = ClientVersionStatus.Status.UPGRADE_RECOMMENDED,
                message = "다양한 기능 사용을 위해 최신 버전으로 업데이트 해주세요."
            )
        }

        return ClientVersionStatus(
            status = ClientVersionStatus.Status.STABLE,
            message = null
        )
    }
}
