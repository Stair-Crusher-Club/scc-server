package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ListConqueredPlacesResponseDto
import club.staircrusher.place_search.application.port.`in`.ListConqueredPlacesUseCase
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ConqueredPlaceController(
    private val listConqueredPlacesUseCase: ListConqueredPlacesUseCase,
) {
    @PostMapping("/listConqueredPlaces")
    fun listConqueredPlaces(
        authentication: SccAppAuthentication
    ): ListConqueredPlacesResponseDto {
        val items = listConqueredPlacesUseCase.handle(authentication.principal)
        // 페이징 로직은 필요할 때 대응한다.
        return ListConqueredPlacesResponseDto(
            totalNumberOfItems = items.count().toLong(),
            nextToken = null,
            items = items.map { it.toDTO() }
        )
    }
}
