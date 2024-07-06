package club.staircrusher.external_accessibility.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ExternalAccessibility
import club.staircrusher.api.spec.dto.GetExternalAccessibilityPostRequest
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPostRequest
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPost200Response
import club.staircrusher.api.spec.dto.ToiletAccessibilityDetails
import club.staircrusher.external_accessibility.application.port.`in`.ExternalAccessibilitySearchService
import club.staircrusher.external_accessibility.application.port.`in`.ExternalAccessibilityService
import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ExternalAccessibilityController(
    private val externalAccessibilitySearchService: ExternalAccessibilitySearchService,
    private val externalAccessibilityService: ExternalAccessibilityService
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
                .map { it.toDTO() }
        )
    }

    @PostMapping("/getExternalAccessibility")
    fun getExternalAccessibility(
        @RequestBody request: GetExternalAccessibilityPostRequest,
    ): ExternalAccessibility {
        return externalAccessibilityService.get(request.externalAccessibilityId).toDTO()
    }

    private fun club.staircrusher.external_accessibility.domain.model.ExternalAccessibility.toDTO(): ExternalAccessibility {
        return ExternalAccessibility(
            id = this.id,
            name = this.name,
            address = this.address,
            location =
            club.staircrusher.api.spec.dto.Location(
                lng = this.location.lng,
                lat = this.location.lat,
            ),
            category = this.category.name,
            toiletDetails = this.toiletDetails?.run {
                ToiletAccessibilityDetails(
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
}
