package club.staircrusher.place.domain.model.check

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class ImageInspectionAttributeConverter : AttributeConverter<ImageInspectionResult, String> {
    private val objectMapper = jacksonObjectMapper()
        .findAndRegisterModules()
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun convertToDatabaseColumn(attribute: ImageInspectionResult?): String? {
        return attribute.let { objectMapper.writeValueAsString(it) } ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): ImageInspectionResult? {
        return try {
            dbData.let { objectMapper.readValue(it, ImageInspectionResult::class.java) }
        } catch (t: Throwable) {
            ImageInspectionResult.NotVisible // FIXME temp
        }
    }
}
