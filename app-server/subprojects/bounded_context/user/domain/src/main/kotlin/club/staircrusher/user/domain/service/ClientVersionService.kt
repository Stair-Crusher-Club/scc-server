package club.staircrusher.user.domain.service

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.version.SemanticVersion

interface ClientVersionService {
    fun getUpgradedNeededVersion(): SemanticVersion
    fun getUpgradedRecommendedVersion(): SemanticVersion
}

@Component
class ClientVersionServiceImpl : ClientVersionService {
    override fun getUpgradedNeededVersion(): SemanticVersion {
        return SemanticVersion(0, 2, 0)
    }

    override fun getUpgradedRecommendedVersion(): SemanticVersion {
        return SemanticVersion(0, 2, 0)
    }
}
