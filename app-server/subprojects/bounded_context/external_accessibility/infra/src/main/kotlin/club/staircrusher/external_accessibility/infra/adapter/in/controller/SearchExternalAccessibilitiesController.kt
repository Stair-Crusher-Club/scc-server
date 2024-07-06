package club.staircrusher.external_accessibility.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ExternalAccessibilityListItem
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPostRequest
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPost200Response
import club.staircrusher.external_accessibility.application.port.`in`.ExternalAccessibilitySearchService
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
                                        request.searchText,
                                        request.currentLocation?.let { Location(lng = it.lng, lat = it.lat) },
                                        Length.ofMeters(request.distanceMetersLimit),
                                )
                                .map {
                                ExternalAccessibilityListItem(
                                        id = it.id,
                                        name = it.name,
                                        address = it.address,
                                        location =
                                                club.staircrusher.api.spec.dto.Location(
                                                        lng = it.longitude,
                                                        lat = it.latitude
                                                ),
                                )
                                }
                )
    }
}
