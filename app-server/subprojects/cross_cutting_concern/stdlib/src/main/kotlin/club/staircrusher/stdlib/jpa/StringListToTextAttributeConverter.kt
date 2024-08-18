package club.staircrusher.stdlib.jpa

import jakarta.persistence.Converter

@Converter
object StringListToTextAttributeConverter : ListToTextAttributeConverter<String>() {
    override fun convertElementToTextColumn(element: String): String {
         return element
    }

    override fun convertElementFromTextColumn(text: String): String {
        return text
    }
}
