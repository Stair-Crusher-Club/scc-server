package club.staircrusher.infra.message_queue

import club.staircrusher.domain.message_queue.Message

data class JacksonSerializedMessage<T: Message>(
    val type: Class<T>,
    val value: String,
)
