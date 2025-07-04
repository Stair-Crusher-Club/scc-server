package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.GetClientVersionStatusRequestDto
import club.staircrusher.api.spec.dto.GetClientVersionStatusResponseDto
import club.staircrusher.stdlib.version.SemanticVersion
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import club.staircrusher.user.domain.service.ClientVersionService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean

class GetClientVersionStatusTest : SccSpringITBase() {

    @MockBean
    lateinit var clientVersionService: ClientVersionService

    @BeforeEach
    fun setUp() {
        Mockito.`when`(
            clientVersionService.getUpgradedNeededVersion()
        ).thenReturn(SemanticVersion(0, 1, 0))
        Mockito.`when`(
            clientVersionService.getUpgradedRecommendedVersion()
        ).thenReturn(SemanticVersion(0, 2, 0))
    }

    @Test
    fun `클라이언트 버전이 너무 낮으면 강제 업데이트를 안내한다`() {
        val status = mvc.sccAnonymousRequest(
            "/getClientVersionStatus",
            GetClientVersionStatusRequestDto(version = "0.0.9")
        )
            .getResult(GetClientVersionStatusResponseDto::class)
            .status
        assertEquals(GetClientVersionStatusResponseDto.Status.UPGRADE_NEEDED, status)
    }

    @Test
    fun `클라이언트 버전이 낮으면 업데이트 추천을 안내한다`() {
        val status = mvc.sccAnonymousRequest(
            "/getClientVersionStatus",
            GetClientVersionStatusRequestDto(version = "0.1.5")
        )
            .getResult(GetClientVersionStatusResponseDto::class)
            .status
        assertEquals(GetClientVersionStatusResponseDto.Status.UPGRADE_RECOMMENDED, status)
    }

}
