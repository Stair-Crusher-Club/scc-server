package club.staircrusher.spring_web.cdn

object SccCdnBeanHolder {
    private var value: SccCdn? = null
    private val monitor = Any()

    fun get(): SccCdn? {
        return value
    }

    fun set(newValue: SccCdn) {
        synchronized(monitor) {
            value = newValue
        }
    }

    fun setIfNull(newValue: SccCdn) {
        synchronized(monitor) {
            if (value == null) {
                value = newValue
            }
        }
    }
}
