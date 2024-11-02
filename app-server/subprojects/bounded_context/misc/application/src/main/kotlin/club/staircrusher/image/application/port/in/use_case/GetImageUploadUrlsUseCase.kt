package club.staircrusher.image.application.port.`in`.use_case

import club.staircrusher.image.application.port.out.file_management.FileManagementService
import club.staircrusher.image.application.port.out.file_management.ImageUploadPurposeType
import club.staircrusher.image.application.port.out.file_management.UploadUrl
import club.staircrusher.stdlib.di.annotation.Component

@Component
class GetImageUploadUrlsUseCase(
    private val fileManagementService: FileManagementService,
) {
    fun handle(
        urlCount: Int,
        filenameExtension: String,
        imageUploadPurposeType: ImageUploadPurposeType,
    ): List<UploadUrl> {
        if (urlCount > urlCountLimit) {
            throw IllegalArgumentException("한 번에 최대 ${urlCountLimit}개의 URL만 생성할 수 있습니다.")
        }
        if (filenameExtension !in allowedFilenameExtensions) {
            throw IllegalArgumentException("${filenameExtension}은 잘못된 확장자입니다. " +
                "$allowedFilenameExtensions 중 하나의 확장자를 지정해주세요.")
        }
        return (0 until urlCount).map {
            fileManagementService.getFileUploadUrl(filenameExtension, imageUploadPurposeType)
        }
    }

    companion object {
        private const val urlCountLimit = 10
        private val allowedFilenameExtensions = setOf("png", "jpg", "jpeg")
    }
}
