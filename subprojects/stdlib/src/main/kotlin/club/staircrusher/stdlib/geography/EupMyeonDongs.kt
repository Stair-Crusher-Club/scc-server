package club.staircrusher.stdlib.geography

import club.staircrusher.stdlib.util.TextResourceReader
import java.security.MessageDigest
import java.util.Base64


val eupMyeonDongById = run {
    val lines = TextResourceReader.read("eupMyeonDong.tsv").split("\n")
    val eupMyeonDongs = lines.map { line ->
        val (siDo, siGunGu, eupMyeonDong) = line.split("\t")
        EupMyeonDong(
            id = getHash("$siDo $siGunGu $eupMyeonDong".toByteArray(), length = 36),
            name = eupMyeonDong,
            siGunGu = SiGunGu(
                id = getHash("$siDo $siGunGu".toByteArray(), length = 36),
                name = siGunGu,
                siDo = siDo,
            ),
        )
    }
    require(eupMyeonDongs.size == eupMyeonDongs.map { it.id }.toSet().size)
    eupMyeonDongs.associateBy { it.id }
}

private fun getHash(byteArray: ByteArray, length: Int? = null): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(byteArray)
    return Base64.getEncoder().encodeToString(md.digest()).let {
        if (length != null) {
            it.take(length)
        } else {
            it
        }
    }
}
