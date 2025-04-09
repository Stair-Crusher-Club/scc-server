package club.staircrusher.place.infra.adapter.`in`.controller.search

import club.staircrusher.api.spec.dto.ListConqueredPlacesRequestDto
import club.staircrusher.api.spec.dto.ListConqueredPlacesResponseDto
import club.staircrusher.place.application.port.`in`.search.ListConqueredPlacesQuery
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ConqueredPlaceController(
    private val listConqueredPlacesQuery: ListConqueredPlacesQuery,
) {
    @PostMapping("/listConqueredPlaces")
    fun listConqueredPlaces(
        @RequestBody request: ListConqueredPlacesRequestDto?,
        authentication: SccAppAuthentication
    ): ListConqueredPlacesResponseDto {
        val result = listConqueredPlacesQuery.listConqueredPlaces(authentication.principal, request?.limit, request?.nextToken)

        return ListConqueredPlacesResponseDto(
            totalNumberOfItems = result.totalCount.toLong(),
            nextToken = result.nextToken,
            items = result.items.map { it.toDTO() }
        )
    }
}
