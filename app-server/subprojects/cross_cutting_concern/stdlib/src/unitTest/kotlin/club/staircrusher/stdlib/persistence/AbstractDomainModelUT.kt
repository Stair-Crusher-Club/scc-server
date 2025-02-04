package club.staircrusher.stdlib.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AbstractDomainModelUT {
    @Test
    fun `정의된 필드가 toString 메소드에 포함된다`() {
        val some = SomeDomainModel(
            id = "id",
            name = "name",
            password = "password",
            age = 10,
            someSensitiveField = "sensitive",
            isClosed = false
        )

        val str = some.toString()
        assertEquals(true, str.contains("id="))
        assertEquals(true, str.contains("name="))
        assertEquals(true, str.contains("password="))
        assertEquals(true, str.contains("age="))
        assertEquals(true, str.contains("someSensitiveField="))
    }

    @Test
    fun `확실한 민감정보는 toString 에서 마스킹된다`() {
        val some = SomeDomainModel(
            id = "id",
            name = "name",
            password = "password",
            age = 10,
            someSensitiveField = "sensitive",
            isClosed = false,
        )

        val str = some.toString()
        assertEquals(true, str.contains("password='<redacted>'"))
    }

    @Test
    fun `Sensitive 어노테이션이 붙은 필드는 toString 에서 마스킹된다`() {
        val some = SomeDomainModel(
            id = "id",
            name = "name",
            password = "password",
            age = 10,
            someSensitiveField = "sensitive",
            isClosed = false,
        )

        val str = some.toString()
        assertEquals(true, str.contains("someSensitiveField='<redacted>'"))
    }

    @Test
    fun `contructor 에 정의되지 않은 필드도 toString 에 포함된다`() {
        val some = SomeDomainModel(
            id = "id",
            name = "name",
            password = "password",
            age = 10,
            someSensitiveField = "sensitive",
            isClosed = false
        )

        val str = some.toString()
        assertEquals(true, str.contains("isClosed='false'"))
    }

    class SomeDomainModel(
        override val id: String,
        val name: String,
        val password: String,
        val age: Int,
        @Sensitive
        val someSensitiveField: String,
        isClosed: Boolean
    ) : AbstractDomainModel() {
        var isClosed: Boolean = isClosed
            private set
    }
}
