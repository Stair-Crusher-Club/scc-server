package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.LoginPostRequest
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.infra.adapter.`in`.controller.base.UserITBase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.ResultActionsDsl

class DeleteUserTest : UserITBase() {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun deleteUserTest() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser(
                password = "password",
            )
        }

        fun login(): ResultActionsDsl {
            return mvc
                .sccRequest("/login", LoginPostRequest(user.nickname, "password"))
        }

        login().andExpect {
            status { isNoContent() }
        }

        mvc
            .sccRequest("/deleteUser", "", user = user)
            .andExpect {
                status { isNoContent() }
                transactionManager.doInTransaction {
                    val deletedUser = userRepository.findById(user.id)
                    assertTrue(deletedUser.isDeleted)
                }
            }

        login().andExpect {
            status { isBadRequest() }
        }
    }
}
