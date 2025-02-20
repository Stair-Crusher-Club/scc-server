package club.staircrusher.quest.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Component
class QuestBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers(): List<RequestMatcher> {
        val pathOnlyMatchers = listOf(
            "/admin/clubQuests",
            "/admin/clubQuests/create/dryRun",
            "/admin/clubQuests/create",
        ).map { AntPathRequestMatcher(it) }
        val pathAndMethodRequestMatchers = listOf(
            AntPathRequestMatcher("/admin/clubQuests/{clubQuestId}", "DELETE")
        )
        return pathOnlyMatchers + pathAndMethodRequestMatchers
    }

    override fun identifiedUserOnlyRequestMatchers() = emptyList<RequestMatcher>()
}
