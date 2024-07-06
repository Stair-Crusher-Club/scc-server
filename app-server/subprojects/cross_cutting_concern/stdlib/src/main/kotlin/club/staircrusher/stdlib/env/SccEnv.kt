package club.staircrusher.stdlib.env

/**
 * 현재 실행 환경이 어디인지를 알기 위한 클래스.
 * properties나 환경변수 등으로부터 현재 실행 환경을 해석한 뒤 setIfNull을 호출하는 코드가 어딘가 있어야 한다.
 *
 * 2024.07 기준 SccEnvSpringInitializer가 설정해준다.
 */
enum class SccEnv {
    TEST,
    LOCAL,
    DEV,
    PROD,
    ;

    companion object {
        private var value: SccEnv? = null
        private val monitor = Any()

        fun setIfNull(newValue: String) {
            if (value != null) {
                return
            }
            synchronized(monitor) {
                if (value != null) {
                    return
                }
                value = SccEnv.values().find { it.name.lowercase() == newValue.lowercase() }
                    ?: throw IllegalArgumentException("Invalid SccEnv: $newValue")
            }
        }

        fun getEnv(): SccEnv {
            return checkNotNull(value) { "SccEnv has not been set." }
        }
    }
}
