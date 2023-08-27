package club.staircrusher.stdlib.validation.email

/**
 * RFC 5322 기반 validation을 진행한다.
 * refs: https://www.baeldung.com/java-email-validation-regex#regular-expression-by-rfc-5322-for-email-validation
 */
object EmailValidator {
    private val regex = Regex("^[a-zA-Z\\d_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z\\d.-]+$")

    fun isValid(email: String): Boolean {
        return regex.matches(email)
    }
}
