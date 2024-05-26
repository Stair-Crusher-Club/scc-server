package club.staircrusher.user.domain.service

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.version.SemanticVersion

@Component
class ClientVersionService {
    fun getUpgradedNeededVersion(): SemanticVersion {
        return SemanticVersion(0, 2, 0)
    }

    fun getUpgradedRecommendedVersion(): SemanticVersion {
        return SemanticVersion(0, 2, 0)
    }
}
