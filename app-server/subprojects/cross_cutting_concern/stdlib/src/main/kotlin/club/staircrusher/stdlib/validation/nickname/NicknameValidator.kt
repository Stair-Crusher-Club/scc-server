package club.staircrusher.stdlib.validation.nickname

object NicknameValidator {
    private const val MIN_LENGTH = 2

    fun isValid(nickname: String?): Boolean {
        if (nickname == null) {
            return true
        }
        val normalizedNickname = nickname.trim()
        return normalizedNickname.length >= MIN_LENGTH
    }

    fun getMinLength(): Int = MIN_LENGTH
} 