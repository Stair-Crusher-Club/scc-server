package club.staircrusher.stdlib.geography

import club.staircrusher.stdlib.util.string.isSimilarWith
import org.junit.jupiter.api.Test

class StringUtilTest {
    @Test
    fun `유사도 확인`() {
        assert("농협하나로마트".isSimilarWith("농협하나로"))
        assert("농협하나로마트".isSimilarWith("농하나로마트"))
        assert("농협하나로마트".isSimilarWith("하나로마트"))
        assert("농협하나로마트".isSimilarWith("농협마트"))
        assert("NonghyupMart".isSimilarWith("NonghyupMart"))
        assert("농협하나로마트".isSimilarWith("농협허너루미틋"))

        assert(!"농협하나로마트".isSimilarWith("아무런"))
        assert(!"농협하나로마트".isSimilarWith("asdf"))
        assert(!"농협하나로마트".isSimilarWith("농협"))
    }
}
