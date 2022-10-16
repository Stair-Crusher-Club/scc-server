package club.staircrusher.stdlib.geography

val siGunGuById = eupMyeonDongById.values.map { it.siGunGu }.toSet().associateBy { it.id }
