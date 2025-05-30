package club.staircrusher.challenge.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Component
class ChallengeBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = listOf(
        "/getChallenge",
        "/listChallenges",

        "/admin/challenges",
        "/admin/challenges/{challengeId}",
    ).map { AntPathRequestMatcher(it) }

    override fun identifiedUserOnlyRequestMatchers() = listOf(
        "/getChallengeWithInvitationCode",
        "/joinChallenge",
    ).map { AntPathRequestMatcher(it) }
}
