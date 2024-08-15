package club.staircrusher.stdlib.jpa

import jakarta.persistence.Converter

@Converter
object IntListToTextAttributeConverter : ListToTextAttributeConverter<Int>() {
    override fun convertElementToTextColumn(element: Int): String {
         return element.toString()
    }

    override fun convertElementFromTextColumn(text: String): Int {
        return text.toInt()
    }
}
