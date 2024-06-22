package club.staircrusher.infra.persistence.sqldelight.column_adapter

import club.staircrusher.accessibility.domain.model.AccessibilityImage

object AccessibilityImageListStringColumnAdapter : ListToTextColumnAdapter<AccessibilityImage>() {
    override fun convertElementToTextColumn(element: AccessibilityImage) = with(element) {
        "${type.name}/$imageUrl/${thumbnailUrl ?: NULL_STRING}"
    }

    override fun convertElementFromTextColumn(text: String): AccessibilityImage {
        val (typeStr, imageUrl, thumbnailUrl) = text.split("/")
        return AccessibilityImage(
            type = AccessibilityImage.Type.valueOf(typeStr),
            imageUrl = imageUrl,
            thumbnailUrl = if (thumbnailUrl == NULL_STRING) null else thumbnailUrl,
        )
    }

    private const val NULL_STRING = "null"
}
