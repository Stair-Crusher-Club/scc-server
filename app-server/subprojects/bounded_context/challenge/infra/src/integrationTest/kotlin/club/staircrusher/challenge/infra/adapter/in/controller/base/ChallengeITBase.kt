package club.staircrusher.challenge.infra.adapter.`in`.controller.base

import club.staircrusher.challenge.domain.model.ChallengeCondition
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import org.springframework.beans.factory.annotation.Autowired
import java.time.Clock
import java.time.ZoneOffset
import kotlin.random.Random

open class ChallengeITBase : SccSpringITBase() {

    @Autowired
    private lateinit var clock: Clock

    fun registerChallenges() {
        val random = Random.nextInt(10)

        repeat(random) {
            testDataGenerator.createChallenge(
                name = "",
                passcode = "",
                startsAt = clock.instant().atOffset(ZoneOffset.ofHours(24)).toInstant(),
                endsAt = clock.instant().atOffset(ZoneOffset.ofHours(24)).toInstant(),
                conditions = listOf(
                    ChallengeCondition(
                        addressMatches = listOf(),
                        accessibilityTypes = listOf()
                    )
                )
            )
        }
    }
}
