package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.GetClientVersionStatusRequestDto
import club.staircrusher.api.spec.dto.GetClientVersionStatusResponseDto
import club.staircrusher.user.application.port.`in`.use_case.GetClientVersionStatusUseCase
import club.staircrusher.user.infra.adapter.`in`.converter.toDTO
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ClientVersionController(
    private val getClientVersionStatusUseCase: GetClientVersionStatusUseCase,
) {
    @PostMapping("/getClientVersionStatus")
    fun getClientVersionStatus(
        @RequestBody request: GetClientVersionStatusRequestDto,
    ): GetClientVersionStatusResponseDto {
        val result = getClientVersionStatusUseCase.handle(request.version)

        return GetClientVersionStatusResponseDto(
            status = result.status.toDTO(),
            message = result.message,
        )
    }
}
