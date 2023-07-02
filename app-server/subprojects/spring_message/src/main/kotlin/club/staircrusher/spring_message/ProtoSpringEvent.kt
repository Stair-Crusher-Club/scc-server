package club.staircrusher.spring_message

import com.squareup.wire.Message
import org.springframework.context.ApplicationEvent

data class ProtoSpringEvent(
    val proto: Message<*, *>,
): ApplicationEvent(proto)
