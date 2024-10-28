package club.staircrusher.image.infra.adapter.`in`.controller

import club.staircrusher.admin_api.converter.toDTO
import club.staircrusher.admin_api.spec.dto.AdminImageUploadPurposeTypeDTO
import club.staircrusher.admin_api.spec.dto.AdminImageUploadUrlDTO
import club.staircrusher.image.application.port.out.file_management.ImageUploadPurposeType
import club.staircrusher.image.application.port.out.file_management.UploadUrl

fun AdminImageUploadPurposeTypeDTO.toModel() = when (this) {
    AdminImageUploadPurposeTypeDTO.BANNER -> ImageUploadPurposeType.BANNER
}

fun UploadUrl.toAdminDTO() = AdminImageUploadUrlDTO(
    url = url,
    expireAt = expireAt.toDTO(),
)
