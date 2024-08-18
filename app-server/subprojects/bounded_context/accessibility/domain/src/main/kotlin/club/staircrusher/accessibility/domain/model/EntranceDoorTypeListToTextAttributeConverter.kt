package club.staircrusher.accessibility.domain.model

import club.staircrusher.stdlib.persistence.jpa.ListToTextAttributeConverter
import jakarta.persistence.Converter

@Converter
object EntranceDoorTypeListToTextAttributeConverter : ListToTextAttributeConverter<EntranceDoorType>() {
    override fun convertElementToTextColumn(element: EntranceDoorType): String {
        return element.name
    }

    override fun convertElementFromTextColumn(text: String): EntranceDoorType {
        return EntranceDoorType.valueOf(text)
    }
}
