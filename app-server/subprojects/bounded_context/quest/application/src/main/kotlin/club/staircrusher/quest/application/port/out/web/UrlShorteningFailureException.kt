package club.staircrusher.quest.application.port.out.web

import club.staircrusher.stdlib.domain.SccDomainException

class UrlShorteningFailureException(msg: String) : SccDomainException(msg)
