package club.staircrusher.user.application.port.out.persistence

import club.staircrusher.user.domain.model.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAccountRepository : CrudRepository<UserAccount, String>
