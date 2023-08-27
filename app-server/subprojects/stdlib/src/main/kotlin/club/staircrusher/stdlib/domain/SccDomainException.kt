package club.staircrusher.stdlib.domain

open class SccDomainException(val msg: String, val errorCode: ErrorCode? = null) : RuntimeException(msg) {
    enum class ErrorCode {
        INVALID_AUTHENTICATION,
        ;
    }

    override fun toString(): String {
        return "SccDomainException(msg='$msg', errorCode=$errorCode)"
    }
}
