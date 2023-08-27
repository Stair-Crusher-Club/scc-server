package club.staircrusher.user.application.port.`in`

import java.util.UUID

object InitialNicknameGenerator {
    fun generate(): String {
        // TODO: human-readable하게 수정
        return UUID.randomUUID().toString().take(16)
    }
}
