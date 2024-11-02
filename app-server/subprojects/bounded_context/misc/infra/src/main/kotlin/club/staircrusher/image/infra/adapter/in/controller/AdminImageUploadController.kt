package club.staircrusher.image.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.AdminCreateImageUploadUrlsRequestDTO
import club.staircrusher.admin_api.spec.dto.AdminCreateImageUploadUrlsResponseDTO
import club.staircrusher.image.application.port.`in`.use_case.GetImageUploadUrlsUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminImageUploadController(
    private val getImageUploadUrlsUseCase: GetImageUploadUrlsUseCase,
) {
    @PostMapping("/admin/image-upload-urls")
    fun adminCreateImageUploadUrls(
        @RequestBody request: AdminCreateImageUploadUrlsRequestDTO,
    ): AdminCreateImageUploadUrlsResponseDTO {
        val urls = getImageUploadUrlsUseCase.handle(
            urlCount = request.count,
            filenameExtension = request.filenameExtension,
            imageUploadPurposeType = request.purposeType.toModel(),
        )
        return AdminCreateImageUploadUrlsResponseDTO(
            urls = urls.map { it.toAdminDTO() }
        )
    }
}
