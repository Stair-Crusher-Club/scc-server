package club.staircrusher.spring_web.jackson

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.ContextualDeserializer

class FallbackEnumDeserializer<T : Enum<T>>(
    private var enumClass: Class<out Enum<*>>? = null,
    private val deserializer: JsonDeserializer<T>?
) : JsonDeserializer<T>(), ContextualDeserializer {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
        try {
            return deserializer?.deserialize(p, ctxt) as T
        } catch (e: Exception) {
            val constants = enumClass!!.enumConstants
            return (constants.firstOrNull { it.name.uppercase() == "UNKNOWN" }
                ?: ctxt.reportInputMismatch(enumClass, "No matching enum and no UNKNOWN fallback.")) as T
        }
    }

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val type = ctxt.contextualType
        val rawClass = type.rawClass as? Class<T>
            ?: throw IllegalStateException("Expected enum class, got: $type")
        return FallbackEnumDeserializer(rawClass, deserializer)
    }
}

class FallbackEnumDeserializerModifier : BeanDeserializerModifier() {
    override fun modifyEnumDeserializer(
        config: DeserializationConfig?,
        type: JavaType?,
        beanDesc: BeanDescription?,
        deserializer: JsonDeserializer<*>?
    ): JsonDeserializer<*>? {
        return FallbackEnumDeserializer(
            type?.rawClass as? Class<out Enum<*>>,
            deserializer as? JsonDeserializer<out Enum<*>>
        )
    }
}

class FallbackEnumModule : SimpleModule() {
    override fun setupModule(context: SetupContext) {
        super.setupModule(context)
        context.addBeanDeserializerModifier(FallbackEnumDeserializerModifier())
    }
}
