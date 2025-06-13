package club.staircrusher.application.message_queue.port.`in`

import club.staircrusher.domain.message_queue.Message

abstract class MessageSubscriber<T> where T : Message {
    protected abstract fun handle(message: T)

    abstract fun canConsume(message: Message): Boolean

    @Suppress("UNCHECKED_CAST")
    operator fun invoke(message: Message) {
        if (canConsume(message)) { handle(message as T) }
    }
}
