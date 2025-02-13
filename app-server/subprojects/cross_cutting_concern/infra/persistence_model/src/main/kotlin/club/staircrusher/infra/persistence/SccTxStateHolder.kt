package club.staircrusher.infra.persistence

object SccTxStateHolder {
    private val current = ThreadLocal.withInitial { State.NONE }!!

    fun get(): State {
        return current.get()
    }

    fun set(state: State) {
        current.set(state)
    }

    fun reset() {
        current.remove()
    }

    enum class State {
        NONE,
        ACTIVE,
        IN_AFTER_COMMIT,
    }
}
