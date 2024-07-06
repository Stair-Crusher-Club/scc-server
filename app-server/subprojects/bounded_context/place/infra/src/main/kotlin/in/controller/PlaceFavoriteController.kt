package `in`.controller

import club.staircrusher.api.spec.dto.CreatePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.CreatePlaceFavoriteResponseDto
import club.staircrusher.api.spec.dto.DeletePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.ListPlaceFavoritesRequestDto
import club.staircrusher.api.spec.dto.ListPlaceFavoritesResponseDto
import club.staircrusher.api.spec.dto.PlaceFavorite
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PlaceFavoriteController(
) {
    @PostMapping("/createPlaceFavorite")
    fun createPlaceFavorite(
        @RequestBody request: CreatePlaceFavoriteRequestDto,
        authentication: SccAppAuthentication?,
    ): CreatePlaceFavoriteResponseDto {
        val userId = authentication?.details?.id ?: throw IllegalArgumentException("Unauthorized")
        // TODO: create place favorite
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
        // TODO: delete place favorite
        return ResponseEntity
            .noContent()
            .build()
    }

    @PostMapping("/listPlaceFavorites")
    fun listPlaceFavorites(
        @RequestBody request: ListPlaceFavoritesRequestDto,
        authentication: SccAppAuthentication?,
    ): ListPlaceFavoritesResponseDto {
        val userId = authentication?.details?.id ?: throw IllegalArgumentException("Unauthorized")
        // TODO: list place favorites
        return ListPlaceFavoritesResponseDto(
            totalNumberOfItems = 0L,
            items = emptyList(),
            nextToken = null
        )
    }
}
