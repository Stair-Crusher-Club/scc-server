package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.UpdateUserInfoPost200Response
import club.staircrusher.api.spec.dto.UpdateUserInfoPostRequest
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.infra.adapter.`in`.converter.toDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userApplicationService: UserApplicationService,
) {
    @PostMapping("/updateUserInfo")
    fun updateUserInfo(
        @RequestBody request: UpdateUserInfoPostRequest,
        authentication: SccAppAuthentication
    ): UpdateUserInfoPost200Response {
        val updatedUser = userApplicationService.updateUserInfo(
            userId = authentication.principal,
            nickname = request.nickname,
            instagramId = request.instagramId,
        )
        return UpdateUserInfoPost200Response(
            user = updatedUser.toDTO(),
        )
    }

    @PostMapping("/deleteUser")
    fun deleteUser(authentication: SccAppAuthentication): ResponseEntity<Unit> {
        userApplicationService.deleteUser(authentication.principal)
        return ResponseEntity.noContent().build()
    }
}
