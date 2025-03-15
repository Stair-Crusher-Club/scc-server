package club.staircrusher.stdlib.persistence

import org.hibernate.Hibernate
import org.hibernate.proxy.HibernateProxy
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

abstract class AbstractDomainModel {
    abstract val id: String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractDomainModel

        return this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }


    override fun toString(): String {
        val properties = this::class.memberProperties
            .filter { it.name != "id" }
            .mapNotNull { prop ->
                try {
                    prop.isAccessible = true
                    val value = prop.getter.call(this)

                    // Exclude uninitialized Hibernate proxy collections
                    if (!isInitialized(value)) return@mapNotNull null

                    val displayValue = if (prop.findAnnotation<Sensitive>() != null || prop.name in sensitiveFields) {
                        "<redacted>"
                    } else {
                        value.toString()
                    }

                    "${prop.name}='$displayValue'"
                } catch (t: Throwable) {
                    null
                }
            }

        return "${this::class.simpleName}(id='$id', ${properties.joinToString(", ")})"
    }

    private fun isInitialized(value: Any?): Boolean {
        return when (value) {
            null -> true
            is Collection<*> -> Hibernate.isInitialized(value)
            is HibernateProxy -> !value.hibernateLazyInitializer.isUninitialized
            else -> true
        }
    }

    companion object {
        private val sensitiveFields = setOf("password", "secretKey", "apiKey", "token")
    }
}
