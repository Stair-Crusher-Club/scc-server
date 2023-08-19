package club.staircrusher.user.application.port.out.web

import club.staircrusher.stdlib.domain.SccDomainException

class InvalidKakaoIdTokenException(msg: String) : SccDomainException(msg)
