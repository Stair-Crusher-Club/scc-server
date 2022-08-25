package club.staircrusher.stdlib.domain.entity

import java.util.UUID

object EntityIdGenerator {
    fun generateRandom(): String {
        return UUID.randomUUID().toString()
    }

    fun generateFixed(key: String): String {
        return UUID.nameUUIDFromBytes(key.toByteArray()).toString()
    }
}
