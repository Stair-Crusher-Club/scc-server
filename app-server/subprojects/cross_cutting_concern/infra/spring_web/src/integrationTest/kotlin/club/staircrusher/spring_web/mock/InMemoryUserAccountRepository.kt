package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.jpa.InMemoryCrudRepositoryMixin
import club.staircrusher.user.application.port.out.persistence.UserAccountRepository
import club.staircrusher.user.domain.model.UserAccount
import org.springframework.context.annotation.Primary

@Component
@Primary
class InMemoryUserAccountRepository : UserAccountRepository, InMemoryCrudRepositoryMixin<UserAccount, String>() {
    override val UserAccount.entityId: String
        get() = this.id
}
