package club.staircrusher.place.infra.adapter.`in`.controller.search

import club.staircrusher.api.spec.dto.ListConqueredPlacesResponseDto
import club.staircrusher.place.application.port.`in`.search.ListConqueredPlacesQuery
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ConqueredPlaceController(
    private val listConqueredPlacesQuery: ListConqueredPlacesQuery,
) {
    @PostMapping("/listConqueredPlaces")
    fun listConqueredPlaces(
        authentication: SccAppAuthentication
    ): ListConqueredPlacesResponseDto {
        val items = listConqueredPlacesQuery.listConqueredPlaces(authentication.principal)
        // 페이징 로직은 필요할 때 대응한다.
        return ListConqueredPlacesResponseDto(
            totalNumberOfItems = items.count().toLong(),
            nextToken = null,
            items = items.map { it.toDTO() }
        )
    }
}
