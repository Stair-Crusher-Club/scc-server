package club.staircrusher.user.application.port.out.web.login.kakao

import club.staircrusher.stdlib.domain.SccDomainException

class InvalidKakaoIdTokenException(msg: String) : SccDomainException(msg, ErrorCode.INVALID_AUTHENTICATION)
