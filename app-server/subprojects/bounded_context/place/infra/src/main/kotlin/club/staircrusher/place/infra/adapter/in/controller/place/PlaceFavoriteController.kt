package club.staircrusher.place.infra.adapter.`in`.controller.place

import club.staircrusher.api.spec.dto.CreatePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.CreatePlaceFavoriteResponseDto
import club.staircrusher.api.spec.dto.DeletePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.DeletePlaceFavoriteResponseDto
import club.staircrusher.api.spec.dto.ListPlaceFavoritesRequestDto
import club.staircrusher.api.spec.dto.ListPlaceFavoritesResponseDto
import club.staircrusher.place.application.port.`in`.place.CreatePlaceFavoriteUseCase
import club.staircrusher.place.application.port.`in`.place.DeletePlaceFavoriteUseCase
import club.staircrusher.place.application.port.`in`.place.ListPlaceFavoritesQuery
import club.staircrusher.place.infra.adapter.`in`.controller.search.toDTO
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PlaceFavoriteController(
    private val createPlaceFavoriteUseCase: CreatePlaceFavoriteUseCase,
    private val deletePlaceFavoriteUseCase: DeletePlaceFavoriteUseCase,
    private val listPlaceFavoritesQuery: ListPlaceFavoritesQuery
) {
    @PostMapping("/createPlaceFavorite")
    fun createPlaceFavorite(
        @RequestBody request: CreatePlaceFavoriteRequestDto,
        authentication: SccAppAuthentication,
    ): CreatePlaceFavoriteResponseDto {
        val userId = authentication.principal
        val response = createPlaceFavoriteUseCase.handle(
            CreatePlaceFavoriteUseCase.Request(
                userId = userId,
                placeId = request.placeId
            )
        )
        return CreatePlaceFavoriteResponseDto(
            totalPlaceFavoriteCount = response.totalPlaceFavoriteCount,
            placeFavorite = response.placeFavorite.toDto()
        )
    }

    @PostMapping("/deletePlaceFavorite")
    fun deletePlaceFavorite(
        @RequestBody request: DeletePlaceFavoriteRequestDto,
        authentication: SccAppAuthentication,
    ): DeletePlaceFavoriteResponseDto {
        val userId = authentication.principal
        val response = deletePlaceFavoriteUseCase.handle(
            DeletePlaceFavoriteUseCase.Request(
                userId = userId,
                placeId = request.placeId
            )
        )
        return DeletePlaceFavoriteResponseDto(totalPlaceFavoriteCount = response.totalPlaceFavoriteCount)
    }

    @PostMapping("/listPlaceFavorites")
    fun listPlaceFavorites(
        @RequestBody request: ListPlaceFavoritesRequestDto,
        authentication: SccAppAuthentication,
    ): ListPlaceFavoritesResponseDto {
        val userId = authentication.principal
        val response = listPlaceFavoritesQuery.handle(
            ListPlaceFavoritesQuery.Request(
                userId = userId,
                limit = request.limit,
                nextToken = request.nextToken
            )
        )
        return ListPlaceFavoritesResponseDto(
            totalNumberOfItems = response.totalCount,
            items = response.places.map { it.toDTO() },
            nextToken = response.nextToken
        )
    }
}
