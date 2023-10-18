package club.staircrusher.infra.persistence.sqldelight.column_adapter

object IntListToTextColumnAdapter : ListToTextColumnAdapter<Int>() {
    override fun convertElementToTextColumn(element: Int): String = element.toString()

    override fun convertElementFromTextColumn(text: String): Int = text.toInt()
}
