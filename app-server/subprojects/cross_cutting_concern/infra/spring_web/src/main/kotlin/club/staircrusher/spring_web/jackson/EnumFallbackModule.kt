package club.staircrusher.spring_web.jackson

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.module.SimpleModule

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer

class FallbackEnumDeserializer<T : Enum<T>>(
    private var enumClass: Class<T>? = null
) : JsonDeserializer<T>(), ContextualDeserializer {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
        val value = p.valueAsString
        val constants = enumClass!!.enumConstants

        return constants.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: constants.firstOrNull { it.name == "UNDEFINED" }
        ?: ctxt.reportInputMismatch(enumClass, "No matching enum and no UNDEFINED fallback.")
    }

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val type = ctxt.contextualType
        val rawClass = type.rawClass as? Class<T>
            ?: throw IllegalStateException("Expected enum class, got: $type")

        return FallbackEnumDeserializer(rawClass)
    }
}

class EnumFallbackModule : SimpleModule() {
    override fun setupModule(context: SetupContext) {
        super.setupModule(context)
        context.addDeserializers(object : Deserializers.Base() {
            override fun findBeanDeserializer(
                type: JavaType, config: DeserializationConfig, beanDesc: BeanDescription
            ): JsonDeserializer<*>? {
                val rawClass = type.rawClass
                return if (rawClass.isEnum) {
                    FallbackEnumDeserializer(rawClass as Class<out Enum<*>>)
                } else null
            }
        })
    }
}
