package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.domain.service.PasswordEncryptor
import org.springframework.context.annotation.Primary

@Component
@Primary
class NoopPasswordEncryptor : PasswordEncryptor {
    override fun encrypt(password: String): String {
        return password
    }

    override fun verify(inputPassword: String, targetPassword: String): Boolean {
        return inputPassword == targetPassword
    }
}
