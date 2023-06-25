package club.staircrusher.user.domain.service

interface PasswordEncryptor {
    fun encrypt(password: String): String
    fun verify(inputPassword: String, targetPassword: String): Boolean
}
