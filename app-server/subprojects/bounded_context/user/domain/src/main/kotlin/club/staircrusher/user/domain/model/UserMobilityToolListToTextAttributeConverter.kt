package club.staircrusher.user.domain.model

import club.staircrusher.stdlib.jpa.ListToTextAttributeConverter
import jakarta.persistence.Converter

@Converter
object UserMobilityToolListToTextAttributeConverter : ListToTextAttributeConverter<UserMobilityTool>() {
    override fun convertElementFromTextColumn(text: String): UserMobilityTool {
        return UserMobilityTool.valueOf(text)
    }
}
