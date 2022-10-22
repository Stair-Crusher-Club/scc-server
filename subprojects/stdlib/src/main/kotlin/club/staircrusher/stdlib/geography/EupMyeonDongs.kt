package club.staircrusher.stdlib.geography

import club.staircrusher.stdlib.util.Hashing
import club.staircrusher.stdlib.util.TextResourceReader


val eupMyeonDongById = run {
    val lines = TextResourceReader.read("eupMyeonDong.tsv").split("\n")
    val eupMyeonDongs = lines.map { line ->
        val (siDo, siGunGu, eupMyeonDong) = line.split("\t")
        EupMyeonDong(
            id = Hashing.getHash("$siDo $siGunGu $eupMyeonDong", length = 36),
            name = eupMyeonDong,
            siGunGu = SiGunGu(
                id = Hashing.getHash("$siDo $siGunGu", length = 36),
                name = siGunGu,
                siDo = siDo,
            ),
        )
    }
    require(eupMyeonDongs.size == eupMyeonDongs.map { it.id }.toSet().size)
    eupMyeonDongs.associateBy { it.id }
}
