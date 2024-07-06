package club.staircrusher.external_accessibility.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ExternalAccessibilityDetails
import club.staircrusher.api.spec.dto.ExternalAccessibilityListItem
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPostRequest
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPost200Response
import club.staircrusher.external_accessibility.application.port.`in`.ExternalAccessibilitySearchService
import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchExternalAccessibilitiesController(
    private val externalAccessibilitySearchService: ExternalAccessibilitySearchService,
) {
    @PostMapping("/searchExternalAccessibilities")
    fun search(
        @RequestBody request: SearchExternalAccessibilitiesPostRequest,
    ): SearchExternalAccessibilitiesPost200Response {
        return SearchExternalAccessibilitiesPost200Response(
            externalAccessibilitySearchService.searchExternalAccessibilities(
                searchText = request.searchText,
                currentLocation = request.currentLocation?.let { Location(lng = it.lng, lat = it.lat) },
                distanceMetersLimit = Length.ofMeters(request.distanceMetersLimit),
                categories = request.categories?.mapNotNull {
                    runCatching { ExternalAccessibilityCategory.valueOf(it) }.getOrNull()
                } ?: emptyList()
            )
                .map {
                    ExternalAccessibilityListItem(
                        id = it.id,
                        name = it.name,
                        address = it.address,
                        location =
                        club.staircrusher.api.spec.dto.Location(
                            lng = it.location.lng,
                            lat = it.location.lat,
                        ),
                        toiletDetails = it.toiletDetails?.run {
                            ExternalAccessibilityDetails(
                                gender = gender,
                                accessDesc = accessDesc,
                                availableDesc = availableDesc,
                                entranceDesc = entranceDesc,
                                stallDesc = stallDesc,
                                doorDesc = doorDesc,
                                washStandDesc = washStandDesc,
                                extra = extra
                            )
                        }
                    )
                }
        )
    }
}
