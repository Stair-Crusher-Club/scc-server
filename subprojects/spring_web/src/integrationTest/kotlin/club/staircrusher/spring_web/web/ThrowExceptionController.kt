package club.staircrusher.spring_web.web

import club.staircrusher.stdlib.domain.SccDomainException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ThrowExceptionController {
    @GetMapping("/throwSccDomainException")
    fun throwSccDomainException() {
        throw SccDomainException("SccDomainException thrown.")
    }
}
