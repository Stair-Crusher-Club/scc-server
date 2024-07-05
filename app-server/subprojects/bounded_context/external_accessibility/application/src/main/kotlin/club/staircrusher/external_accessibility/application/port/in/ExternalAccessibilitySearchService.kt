package club.staircrusher.external_accessibility.application.port.`in`

import club.staircrusher.external_accessibility.application.port.out.persistence.ExternalAccessibilityRepository
import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location

@Component
class ExternalAccessibilitySearchService(
        private val externalAccessibilityRepository: ExternalAccessibilityRepository,
) {
    fun searchExternalAccessibilities(
        searchText: String?,
        currentLocation: Location?,
        distanceMetersLimit: Length,
    ): List<ExternalAccessibility> {
        return externalAccessibilityRepository
            .findAll()
            .filter {
                currentLocation ?: return@filter true
                calcDistance(currentLocation, Location(it.longitude, it.latitude)).meter <= distanceMetersLimit.meter
            }
            .filter { 
                searchText ?: return@filter true
                levenshteinDistance(searchText, it.name) <= 1 
            }
    }

    private fun calcDistance(l1: Location, l2: Location): Length {
        val earthRadius = 6371.0 // Earth radius in kilometers
        val lat1 = l1.lat
        val lat2 = l2.lat
        val lon1 = l1.lng
        val lon2 = l2.lng

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(lat1)) *
                                Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon / 2) *
                                Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return Length(earthRadius * c)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) {
            for (j in 0..s2.length) {
                dp[i][j] =
                        when {
                            i == 0 -> j
                            j == 0 -> i
                            else ->
                                    minOf(
                                            dp[i - 1][j - 1] + if (s1[i - 1] == s2[j - 1]) 0 else 1,
                                            dp[i - 1][j] + 1,
                                            dp[i][j - 1] + 1
                                    )
                        }
            }
        }
        return dp[s1.length][s2.length]
    }
}
