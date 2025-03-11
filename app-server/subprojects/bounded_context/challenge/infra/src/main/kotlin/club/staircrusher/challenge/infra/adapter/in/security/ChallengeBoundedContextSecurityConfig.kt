package club.staircrusher.challenge.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

class ChallengeBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = listOf(
        "/getChallenge",
        "/listChallenges",
    ).map { AntPathRequestMatcher(it) }

    override fun identifiedUserOnlyRequestMatchers() = listOf(
        "/getChallengeWithInvitationCode",
        "/joinChallenge",
    ).map { AntPathRequestMatcher(it) }
}
