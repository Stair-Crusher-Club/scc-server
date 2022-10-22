package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.file_management.UploadUrl
import club.staircrusher.stdlib.di.annotation.Component

@Component
class GetImageUploadUrlsUseCase(
    private val fileManagementService: FileManagementService,
) {
    fun handle(
        urlCount: Int,
        filenameExtension: String,
    ): List<UploadUrl> {
        if (urlCount > urlCountLimit) {
            throw IllegalArgumentException("한 번에 최대 ${urlCountLimit}개의 URL만 생성할 수 있습니다.")
        }
        return (0 until urlCount).map {
            fileManagementService.getFileUploadUrl(filenameExtension)
        }
    }

    companion object {
        private const val urlCountLimit = 10
    }
}
