package club.staircrusher.user.infra.service

import at.favre.lib.crypto.bcrypt.BCrypt
import club.staircrusher.user.domain.service.PasswordEncryptor

object BCryptPasswordEncryptor : PasswordEncryptor {
    private val bcrypt = BCrypt.withDefaults()
    private val verifier = BCrypt.verifyer()

    override fun encrypt(password: String): String {
        return bcrypt.hashToString(8, password.toCharArray())
    }

    override fun verify(inputPassword: String, targetPassword: String): Boolean {
        return verifier.verify(inputPassword.toCharArray(), targetPassword.toCharArray()).verified
    }
}
