package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.admin_api.spec.dto.AdminAccessibilityDTO
import club.staircrusher.admin_api.spec.dto.AdminSearchAccessibilitiesResultDTO
import club.staircrusher.admin_api.spec.dto.AdminUpdateBuildingAccessibilityRequestDTO
import club.staircrusher.admin_api.spec.dto.AdminUpdatePlaceAccessibilityRequestDTO
import club.staircrusher.place.application.port.`in`.accessibility.AdminDeleteBuildingAccessibilityUseCase
import club.staircrusher.place.application.port.`in`.accessibility.AdminDeletePlaceAccessibilityUseCase
import club.staircrusher.place.application.port.`in`.accessibility.AdminSearchAccessibilitiesUseCase
import club.staircrusher.place.application.port.`in`.accessibility.AdminUpdateBuildingAccessibilityUseCase
import club.staircrusher.place.application.port.`in`.accessibility.AdminUpdatePlaceAccessibilityUseCase
import club.staircrusher.stdlib.util.string.emptyToNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class AdminAccessibilityController(
    private val adminSearchAccessibilitiesUseCase: AdminSearchAccessibilitiesUseCase,
    private val adminUpdatePlaceAccessibilityUseCase: AdminUpdatePlaceAccessibilityUseCase,
    private val adminUpdateBuildingAccessibilityUseCase: AdminUpdateBuildingAccessibilityUseCase,
    private val adminDeletePlaceAccessibilityUseCase: AdminDeletePlaceAccessibilityUseCase,
    private val adminDeleteBuildingAccessibilityUseCase: AdminDeleteBuildingAccessibilityUseCase,
) {
    @GetMapping("/admin/accessibilities/search")
    fun searchAccessibilities(
        @RequestParam(required = false) placeName: String?,
        @RequestParam(required = false) createdAtFromLocalDate: String?,
        @RequestParam(required = false) createdAtToLocalDate: String?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false) limit: Int?,
    ): AdminSearchAccessibilitiesResultDTO {
        val result = adminSearchAccessibilitiesUseCase.handle(
            placeName = placeName,
            createdAtFromLocalDate = createdAtFromLocalDate?.emptyToNull()?.let { LocalDate.parse(it) },
            createdAtToLocalDate = createdAtToLocalDate?.emptyToNull()?.let { LocalDate.parse(it) },
            cursorValue = cursor,
            limit = limit,
        )
        return AdminSearchAccessibilitiesResultDTO(
            items = result.items.map {
                AdminAccessibilityDTO(
                    placeAccessibility = it.placeAccessibility.toAdminDTO(
                        placeName = it.place.name,
                        registeredUserName = it.placeAccessibilityRegisteredUser?.nickname,
                    ),
                    buildingAccessibility = it.buildingAccessibility?.toAdminDTO(
                        buildingName = it.place.building.name,
                        registeredUserName = it.buildingAccessibilityRegisteredUser?.nickname,
                    )

                )
            },
            cursor = result.cursor,
        )
    }

    @PutMapping("/admin/place-accessibilities/{id}")
    fun updatePlaceAccessibility(@PathVariable id: String, @RequestBody request: AdminUpdatePlaceAccessibilityRequestDTO): ResponseEntity<Unit> {
        adminUpdatePlaceAccessibilityUseCase.handle(
            placeAccessibilityId = id,
            isFirstFloor = request.isFirstFloor,
            stairInfo = request.stairInfo.toModel(),
            hasSlope = request.hasSlope,
            floors = request.floors,
            isStairOnlyOption = request.isStairOnlyOption,
            stairHeightLevel = request.stairHeightLevel?.toModel(),
            entranceDoorTypes = request.entranceDoorTypes?.map { it.toModel() },
        )
        return ResponseEntity
            .noContent()
            .build()
    }

    @PutMapping("/admin/building-accessibilities/{id}")
    fun updateBuildingAccessibility(@PathVariable id: String, @RequestBody request: AdminUpdateBuildingAccessibilityRequestDTO): ResponseEntity<Unit> {
        adminUpdateBuildingAccessibilityUseCase.handle(
            buildingAccessibilityId = id,
            hasElevator = request.hasElevator,
            hasSlope = request.hasSlope,
            entranceStairInfo = request.entranceStairInfo.toModel(),
            entranceStairHeightLevel = request.entranceStairHeightLevel?.toModel(),
            entranceDoorTypes = request.entranceDoorTypes?.map { it.toModel() },
            elevatorStairInfo = request.elevatorStairInfo.toModel(),
            elevatorStairHeightLevel = request.elevatorStairHeightLevel?.toModel(),
        )
        return ResponseEntity
            .noContent()
            .build()
    }

    @DeleteMapping("/admin/place-accessibilities/{id}")
    fun deletePlaceAccessibility(
        @PathVariable id: String,
    ): ResponseEntity<Unit> {
        adminDeletePlaceAccessibilityUseCase.handle(placeAccessibilityId = id)
        return ResponseEntity
            .noContent()
            .build()
    }

    @DeleteMapping("/admin/building-accessibilities/{id}")
    fun deleteBuildingAccessibility(
        @PathVariable id: String,
    ): ResponseEntity<Unit> {
        adminDeleteBuildingAccessibilityUseCase.handle(buildingAccessibilityId = id)
        return ResponseEntity
            .noContent()
            .build()
    }
}
