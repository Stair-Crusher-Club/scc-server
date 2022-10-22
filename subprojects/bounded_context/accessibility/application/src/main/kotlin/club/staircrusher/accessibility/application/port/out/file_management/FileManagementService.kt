package club.staircrusher.accessibility.application.port.out.file_management

interface FileManagementService {
    fun getFileUploadUrl(filenameExtension: String): UploadUrl
}
