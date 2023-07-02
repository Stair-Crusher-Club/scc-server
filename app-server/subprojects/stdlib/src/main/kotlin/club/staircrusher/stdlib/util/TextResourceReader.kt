package club.staircrusher.stdlib.util

import java.nio.charset.Charset

object TextResourceReader {
    fun read(resourceName: String, charset: Charset = Charsets.UTF_8): String {
        return this.javaClass.classLoader.getResource(resourceName)!!.readText(charset = charset)
    }
}
