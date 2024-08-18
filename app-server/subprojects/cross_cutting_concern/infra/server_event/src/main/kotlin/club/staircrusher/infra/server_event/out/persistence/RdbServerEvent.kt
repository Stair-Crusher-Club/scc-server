package club.staircrusher.infra.server_event.out.persistence

import club.staircrusher.domain.server_event.ServerEvent
import club.staircrusher.domain.server_event.ServerEventPayload
import club.staircrusher.domain.server_event.ServerEventType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "server_event")
class RdbServerEvent(
    @Id
    val id: String,
    @Enumerated(EnumType.STRING)
    val type: ServerEventType,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "TEXT")
    val payload: ServerEventPayload,
    val createdAt: Instant,
) {
    constructor(serverEvent: ServerEvent) : this(
        id = serverEvent.id,
        type = serverEvent.type,
        payload = serverEvent.payload,
        createdAt = serverEvent.createdAt,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RdbServerEvent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "RdbServerEvent(id='$id', type=$type, payload=$payload, createdAt=$createdAt)"
    }
}
