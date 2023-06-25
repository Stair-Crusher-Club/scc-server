package club.staircrusher.stdlib.token

class TokenVerificationException(msg: String) : RuntimeException(msg) {
    constructor() : this("") // FIXME: 없으면 kopy-kat 관련해서 에러가 난다.
}
