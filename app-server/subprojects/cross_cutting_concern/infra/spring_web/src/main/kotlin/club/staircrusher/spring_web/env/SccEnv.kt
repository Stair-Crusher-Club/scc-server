package club.staircrusher.spring_web.env

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.beans.factory.annotation.Value

@Component
open class SccEnv(
    @Value("\${scc.environment:dev}") val env: String,
) {
    init {
        SccEnvBeanHolder.setIfNull(this)
    }

    fun isDev(): Boolean {
        return env == "dev"
    }

    companion object {
        fun isDev(): Boolean {
            val globalSccEnv = SccEnvBeanHolder.get()
            checkNotNull(globalSccEnv) {
                "Cannot use SccEnv.isDev() since SccEnv bean is not initialized yet."
            }
            return globalSccEnv.isDev()
        }
    }
}
