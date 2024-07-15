package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.CreatePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.CreatePlaceFavoriteResponseDto
import club.staircrusher.api.spec.dto.DeletePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.DeletePlaceFavoriteResponseDto
import club.staircrusher.api.spec.dto.ListPlaceFavoritesByUserRequestDto
import club.staircrusher.api.spec.dto.ListPlaceFavoritesByUserResponseDto
import club.staircrusher.place.application.port.`in`.CreatePlaceFavoriteUseCase
import club.staircrusher.place.application.port.`in`.DeletePlaceFavoriteUseCase
import club.staircrusher.place.application.port.`in`.ListPlaceFavoritesByUserUseCase
import club.staircrusher.place.infra.adapter.out.persistence.toDto
import club.staircrusher.spring_web.security.app.SccAppAuthentication
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
    fun deletePlaceFavorites(
        @RequestBody request: DeletePlaceFavoriteRequestDto,
        authentication: SccAppAuthentication?,
    ): DeletePlaceFavoriteResponseDto {
        val userId = authentication?.details?.id ?: throw IllegalArgumentException("Unauthorized")
        val response = deletePlaceFavoriteUseCase.handle(
            DeletePlaceFavoriteUseCase.Request(
                userId = userId,
                placeId = request.placeId
            )
        )
        return DeletePlaceFavoriteResponseDto(totalPlaceFavoriteCount = response.totalPlaceFavoriteCount)
    }

    @PostMapping("/listPlaceFavoritesByUser")
    fun listPlaceFavorites(
        @RequestBody request: ListPlaceFavoritesByUserRequestDto,
        authentication: SccAppAuthentication?,
    ): ListPlaceFavoritesByUserResponseDto {
        val userId = authentication?.details?.id ?: throw IllegalArgumentException("Unauthorized")
        val response = listPlaceFavoritesByUserUseCase.handle(
            ListPlaceFavoritesByUserUseCase.Request(
                userId = userId,
                limit = request.limit,
                nextToken = request.nextToken
            )
        )
        return ListPlaceFavoritesByUserResponseDto(
            totalNumberOfItems = response.totalCount,
            items = response.favorites.map { it.toDto() },
            nextToken = response.nextToken
        )
    }
}
