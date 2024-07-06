package club.staircrusher.spring_web.env

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.env.SccEnv
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value

@Component
class SccEnvSpringInitializer(
    @Value("\${scc.environment:local}") val env: String,
): InitializingBean {
    override fun afterPropertiesSet() {
        SccEnv.setIfNull(env)
    }
}

