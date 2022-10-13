package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.place_search.application.port.`in`.ListConqueredPlacesQuery
import club.staircrusher.spring_web.authentication.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ConqueredPlaceController(
    private val listConqueredPlacesQuery: ListConqueredPlacesQuery,
) {
    @PostMapping("/listConqueredPlaces")
    fun listConqueredPlaces(authentication: SccAppAuthentication): List<PlaceListItem> {
        return listConqueredPlacesQuery.listConqueredPlaces(authentication.principal).map { it.toDTO() }
    }
}
