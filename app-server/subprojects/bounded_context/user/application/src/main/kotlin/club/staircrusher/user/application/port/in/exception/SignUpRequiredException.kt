package club.staircrusher.user.application.port.`in`.exception

import club.staircrusher.stdlib.domain.SccDomainException

class SignUpRequiredException(msg: String) : SccDomainException(msg)
