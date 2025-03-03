package club.staircrusher.stdlib.domain

open class SccDomainException(
    val msg: String,
    val errorCode: ErrorCode? = null,
    cause: Throwable? = null,
) : RuntimeException(msg, cause) {
    enum class ErrorCode {
        INVALID_AUTHENTICATION,
        INVALID_NICKNAME,
        INVALID_EMAIL,
        INVALID_BIRTH_YEAR,
        INVALID_PASSCODE,
        INVALID_ARGUMENTS,
        ALREADY_JOINED,
        CHALLENGE_NOT_OPENED,
        CHALLENGE_CLOSED
        ;
    }

    override fun toString(): String {
        return "SccDomainException(msg='$msg', errorCode=$errorCode)"
    }
}
