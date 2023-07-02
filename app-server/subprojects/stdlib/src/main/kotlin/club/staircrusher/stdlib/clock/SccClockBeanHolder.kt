package club.staircrusher.stdlib.clock

object SccClockBeanHolder {
    private var value: SccClock? = null
    private val monitor = Any()

    fun get(): SccClock? {
        return value
    }

    fun set(newValue: SccClock) {
        synchronized(monitor) {
            value = newValue
        }
    }

    fun setIfNull(newValue: SccClock) {
        synchronized(monitor) {
            if (value == null) {
                value = newValue
            }
        }
    }
}
