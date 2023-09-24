package club.staircrusher.user.application.port.out.web.login.apple

import club.staircrusher.stdlib.domain.SccDomainException

class InvalidAppleIdTokenException(msg: String) : SccDomainException(msg, ErrorCode.INVALID_AUTHENTICATION)
