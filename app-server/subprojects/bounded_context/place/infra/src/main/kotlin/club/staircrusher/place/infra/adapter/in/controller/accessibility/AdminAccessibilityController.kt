package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.admin_api.spec.dto.AdminAccessibilityDTO
import club.staircrusher.admin_api.spec.dto.AdminAccessibilityImageDTO
import club.staircrusher.admin_api.spec.dto.AdminAccessibilityImageDTOInspectionResult
import club.staircrusher.admin_api.spec.dto.AdminSearchAccessibilitiesResultDTO
import club.staircrusher.admin_api.spec.dto.AdminSearchAccessibilityImagesResultDTO
import club.staircrusher.admin_api.spec.dto.AdminUpdateBuildingAccessibilityRequestDTO
import club.staircrusher.admin_api.spec.dto.AdminUpdatePlaceAccessibilityRequestDTO
import club.staircrusher.admin_api.spec.dto.EpochMillisTimestamp
import club.staircrusher.place.application.port.`in`.accessibility.AdminDeleteBuildingAccessibilityUseCase
import club.staircrusher.place.application.port.`in`.accessibility.AdminDeletePlaceAccessibilityUseCase
import club.staircrusher.place.application.port.`in`.accessibility.AdminSearchAccessibilitiesUseCase
import club.staircrusher.place.application.port.`in`.accessibility.AdminSearchAccessibilityImagesUseCase
import club.staircrusher.place.application.port.`in`.accessibility.AdminUpdateBuildingAccessibilityUseCase
import club.staircrusher.place.application.port.`in`.accessibility.AdminUpdatePlaceAccessibilityUseCase
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.check.ImageInspectionResult
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
    private val adminSearchAccessibilityImagesUseCase: AdminSearchAccessibilityImagesUseCase,
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

    @GetMapping("/admin/accessibilty-images/search")
    fun searchAccessibilityImages(
        @RequestParam(required = false) inspectionResultType: String?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false) limit: Int?,
    ): AdminSearchAccessibilityImagesResultDTO {
        val result = adminSearchAccessibilityImagesUseCase.handle(
            inspectionResultType = inspectionResultType,
            cursorValue = cursor,
            limit = limit,
        )
        return AdminSearchAccessibilityImagesResultDTO(
            items = result.items.map { image ->
                val accessibilityType = when (image.accessibilityType) {
                    AccessibilityImage.AccessibilityType.Place -> AdminAccessibilityImageDTO.AccessibilityType.PLACE
                    AccessibilityImage.AccessibilityType.Building -> AdminAccessibilityImageDTO.AccessibilityType.BUILDING
                }

                val imageType = image.imageType?.let { imageType ->
                    when (imageType) {
                        AccessibilityImage.ImageType.Entrance -> AdminAccessibilityImageDTO.ImageType.ENTRANCE
                        AccessibilityImage.ImageType.Elevator -> AdminAccessibilityImageDTO.ImageType.ELEVATOR
                    }
                }

                val inspectionResult = image.inspectionResult?.let {
                    when (it) {
                        is ImageInspectionResult.Visible -> {
                            val objects = it.objects.map { obj ->
                                when (obj) {
                                    ImageInspectionResult.DetectedObject.Elevator -> AdminAccessibilityImageDTOInspectionResult.Objects.ELEVATOR
                                    ImageInspectionResult.DetectedObject.Entrance -> AdminAccessibilityImageDTOInspectionResult.Objects.ENTRANCE
                                    ImageInspectionResult.DetectedObject.Stair -> AdminAccessibilityImageDTOInspectionResult.Objects.STAIR
                                }
                            }
                            val rotation = when (it.rotation) {
                                ImageInspectionResult.Rotation.D0 -> AdminAccessibilityImageDTOInspectionResult.Rotation.D0
                                ImageInspectionResult.Rotation.D90 -> AdminAccessibilityImageDTOInspectionResult.Rotation.D90
                                ImageInspectionResult.Rotation.D180 -> AdminAccessibilityImageDTOInspectionResult.Rotation.D180
                                ImageInspectionResult.Rotation.D270 -> AdminAccessibilityImageDTOInspectionResult.Rotation.D270
                            }
                            AdminAccessibilityImageDTOInspectionResult(
                                type = AdminAccessibilityImageDTOInspectionResult.Type.VISIBLE,
                                objects = objects,
                                rotation = rotation
                            )
                        }
                        is ImageInspectionResult.NotVisible -> {
                            val rotation = when (it.rotation) {
                                ImageInspectionResult.Rotation.D0 -> AdminAccessibilityImageDTOInspectionResult.Rotation.D0
                                ImageInspectionResult.Rotation.D90 -> AdminAccessibilityImageDTOInspectionResult.Rotation.D90
                                ImageInspectionResult.Rotation.D180 -> AdminAccessibilityImageDTOInspectionResult.Rotation.D180
                                ImageInspectionResult.Rotation.D270 -> AdminAccessibilityImageDTOInspectionResult.Rotation.D270
                            }
                            AdminAccessibilityImageDTOInspectionResult(
                                type = AdminAccessibilityImageDTOInspectionResult.Type.NOT_VISIBLE,
                                rotation = rotation
                            )
                        }
                    }
                }

                AdminAccessibilityImageDTO(
                    id = image.id,
                    accessibilityId = image.accessibilityId,
                    accessibilityType = accessibilityType,
                    originalImageUrl = image.originalImageUrl,
                    blurredImageUrl = image.blurredImageUrl,
                    thumbnailUrl = image.thumbnailUrl,
                    imageType = imageType,
                    inspectionResult = inspectionResult,
                    createdAt = EpochMillisTimestamp(image.createdAt.toEpochMilli()),
                    displayOrder = image.displayOrder
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
