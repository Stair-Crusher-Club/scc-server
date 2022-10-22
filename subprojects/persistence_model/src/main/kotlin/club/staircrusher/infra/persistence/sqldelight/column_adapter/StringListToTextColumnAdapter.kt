package club.staircrusher.infra.persistence.sqldelight.column_adapter

object StringListToTextColumnAdapter : ListToTextColumnAdapter<String>() {
    override fun convertElementToTextColumn(element: String) = element
    override fun convertElementFromTextColumn(text: String) = text
}
