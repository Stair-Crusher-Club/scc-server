package club.staircrusher.user.infra.service

import at.favre.lib.crypto.bcrypt.BCrypt
import club.staircrusher.user.domain.service.PasswordEncryptor
import club.staircrusher.stdlib.di.annotation.Component

@Component
object BCryptPasswordEncryptor : PasswordEncryptor {
    private const val COST = 8
    private val bcrypt = BCrypt.withDefaults()
    private val verifier = BCrypt.verifyer()

    override fun encrypt(password: String): String {
        return bcrypt.hashToString(COST, password.toCharArray())
    }

    override fun verify(inputPassword: String, targetPassword: String): Boolean {
        return verifier.verify(inputPassword.toCharArray(), targetPassword.toCharArray()).verified
    }
}
