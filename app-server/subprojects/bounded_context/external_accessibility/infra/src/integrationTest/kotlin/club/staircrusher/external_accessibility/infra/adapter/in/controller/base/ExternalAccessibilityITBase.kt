package club.staircrusher.external_accessibility.infra.adapter.`in`.controller.base

import club.staircrusher.testing.spring_it.base.SccSpringITBase
import java.time.Clock
import org.springframework.beans.factory.annotation.Autowired

open class ExternalAccessibilityITBase : SccSpringITBase() {
    @Autowired private lateinit var clock: Clock
}
