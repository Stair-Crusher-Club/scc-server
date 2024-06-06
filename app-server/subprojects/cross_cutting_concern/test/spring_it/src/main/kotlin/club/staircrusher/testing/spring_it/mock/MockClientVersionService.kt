package club.staircrusher.testing.spring_it.mock

import club.staircrusher.stdlib.version.SemanticVersion
import club.staircrusher.user.domain.service.ClientVersionService

open class MockClientVersionService : ClientVersionService {
    override fun getUpgradedNeededVersion(): SemanticVersion {
        return SemanticVersion(0, 2, 0)
    }

    override fun getUpgradedRecommendedVersion(): SemanticVersion {
        return SemanticVersion(0, 2, 0)
    }
}
