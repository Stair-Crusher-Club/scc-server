package club.staircrusher.domain.server_event

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
object ServerEventPayloadToTextAttributeConverter : AttributeConverter<ServerEventPayload, String> {
    override fun convertToDatabaseColumn(eventPayload: ServerEventPayload?): String {
        return eventPayload?.let { objectMapper.writeValueAsString(it) } ?: ""
    }

    override fun convertToEntityAttribute(databaseValue: String?): ServerEventPayload? {
        return try {
            databaseValue?.let { objectMapper.readValue<ServerEventPayload>(it) }
        } catch (t: Throwable) {
            UnknownServerEventPayload
        }
    }

    @JvmStatic
    private val objectMapper = jacksonObjectMapper()
        .findAndRegisterModules()
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}
