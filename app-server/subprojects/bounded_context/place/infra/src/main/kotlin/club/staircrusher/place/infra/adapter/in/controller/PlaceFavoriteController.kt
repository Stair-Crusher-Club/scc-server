package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.CreatePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.CreatePlaceFavoriteResponseDto
import club.staircrusher.api.spec.dto.DeletePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.ListPlaceFavoritesRequestDto
import club.staircrusher.api.spec.dto.ListPlaceFavoritesResponseDto
import club.staircrusher.api.spec.dto.PlaceFavorite
import club.staircrusher.place.application.port.`in`.CreatePlaceFavoriteUseCase
import club.staircrusher.place.application.port.`in`.DeletePlaceFavoriteUseCase
import club.staircrusher.place.application.port.`in`.ListPlaceFavoritesByUserUseCase
import club.staircrusher.place.infra.adapter.out.persistence.toDto
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PlaceFavoriteController(
    private val createPlaceFavoriteUseCase: CreatePlaceFavoriteUseCase,
    private val deletePlaceFavoriteUseCase: DeletePlaceFavoriteUseCase,
    private val listPlaceFavoritesByUserUseCase: ListPlaceFavoritesByUserUseCase
) {
    @PostMapping("/createPlaceFavorite")
    fun createPlaceFavorite(
        @RequestBody request: CreatePlaceFavoriteRequestDto,
        authentication: SccAppAuthentication?,
    ): CreatePlaceFavoriteResponseDto {
        val userId = authentication?.details?.id ?: throw IllegalArgumentException("Unauthorized")
        createPlaceFavoriteUseCase.handle(
            CreatePlaceFavoriteUseCase.Request(
                userId = userId,
                placeId = request.placeId
            )
        )
        return CreatePlaceFavoriteResponseDto(
            placeFavorite = PlaceFavorite(
                placeId = request.placeId,
                userId = userId,
            )
        )
    }

    @PostMapping("/deletePlaceFavorite")
    fun deletePlaceFavorites(
        @RequestBody request: DeletePlaceFavoriteRequestDto,
        authentication: SccAppAuthentication?,
    ): ResponseEntity<Unit> {
        val userId = authentication?.details?.id ?: throw IllegalArgumentException("Unauthorized")
        deletePlaceFavoriteUseCase.handle(
            DeletePlaceFavoriteUseCase.Request(
                userId = userId,
                placeId = request.placeId
            )
        )
        return ResponseEntity
            .noContent()
            .build()
    }

    @PostMapping("/listPlaceFavoritesByUser")
    fun listPlaceFavorites(
        @RequestBody request: ListPlaceFavoritesRequestDto,
        authentication: SccAppAuthentication?,
    ): ListPlaceFavoritesResponseDto {
        val userId = authentication?.details?.id ?: throw IllegalArgumentException("Unauthorized")
        val response = listPlaceFavoritesByUserUseCase.handle(
            ListPlaceFavoritesByUserUseCase.Request(
                userId = userId,
                limit = request.limit,
                nextToken = request.nextToken
            )
        )
        return ListPlaceFavoritesResponseDto(
            totalNumberOfItems = response.totalCount,
            items = response.favorites.map { it.toDto() },
            nextToken = response.nextToken
        )
    }
}
