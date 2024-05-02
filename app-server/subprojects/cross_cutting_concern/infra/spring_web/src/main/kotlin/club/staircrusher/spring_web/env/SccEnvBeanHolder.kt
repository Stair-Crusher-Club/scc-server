package club.staircrusher.spring_web.env

object SccEnvBeanHolder {
    private var value: SccEnv? = null
    private val monitor = Any()

    fun get(): SccEnv? {
        return value
    }

    fun set(newValue: SccEnv) {
        synchronized(monitor) {
            value = newValue
        }
    }

    fun setIfNull(newValue: SccEnv) {
        synchronized(monitor) {
            if (value == null) {
                value = newValue
            }
        }
    }
}
