package club.staircrusher.stdlib.persistence

import club.staircrusher.stdlib.jpa.ListToTextAttributeConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ListToTextAttributeConverterUT {
    object SomeEnumListToTextAttributeConverter : ListToTextAttributeConverter<SomeEnum>() {
        override fun convertElementToTextColumn(element: SomeEnum): String {
            return element.name
        }

        override fun convertElementFromTextColumn(text: String): SomeEnum {
            return SomeEnum.valueOf(text)
        }
    }

    enum class SomeEnum {
        A, B, C, D, E
    }

    private val sut = SomeEnumListToTextAttributeConverter

    @Test
    fun `기본 동작 테스트`() {
        val attribute = listOf(SomeEnum.A, SomeEnum.B, SomeEnum.D, SomeEnum.B)
        val deserialized = sut.convertToDatabaseColumn(attribute)
        assertEquals("A,,B,,D,,B", deserialized)

        val serialized = sut.convertToEntityAttribute(deserialized)
        assertEquals(attribute, serialized)
    }

    @Test
    fun `db 에 null 이 저장된 케이스도 잘 처리해준다`() {
        val serialized = sut.convertToEntityAttribute(null)
        assertTrue(serialized.isEmpty())
    }

    // FIXME: json으로 바꾸기
//    @Test
//    fun `기본 동작 테스트`() {
//        val attribute = listOf(SomeEnum.A, SomeEnum.B, SomeEnum.D, SomeEnum.B)
//        val deserialized = sut.convertToDatabaseColumn(attribute)
//        assertEquals("[\"A\",\"B\",\"D\",\"B\"]", deserialized)
//
//        val serialized = sut.convertToEntityAttribute(deserialized)
//        assertEquals(attribute, serialized)
//    }
//
//    @Test
//    fun `하위호환 테스트`() {
//        val attribute = listOf(SomeEnum.A, SomeEnum.B, SomeEnum.D, SomeEnum.B)
//        val legacy = attribute.joinToString(ListToTextAttributeConverter.LEGACY_DELIMITER)
//
//        val serialized = sut.convertToEntityAttribute(legacy)
//        assertEquals(attribute, serialized)
//
//        val deserialized = sut.convertToDatabaseColumn(serialized)
//        assertNotEquals(legacy, deserialized)
//        assertEquals("[\"A\",\"B\",\"D\",\"B\"]", deserialized)
//    }
}
