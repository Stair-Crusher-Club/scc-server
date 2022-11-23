package club.staircrusher.quest.util

@Suppress("MagicNumber")
object HumanReadablePrefixGenerator {
    fun generateByAlphabet(idx: Int): String {
        check(idx >= 0)
        return if (idx > 25) {
            "${generateByAlphabet(idx / 26 - 1)}${generateSingle(idx % 26)}"
        } else {
            generateSingle(idx).toString()
        }
    }

    private fun generateSingle(idx: Int): Char {
        check(idx in 0..25)
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ"[idx]
    }

    fun generateByWord(idx: Int): String {
        check(idx < words.size) { "가진 단어 목록 수(${words.size}개)가 부족합니다." }
        return words[idx]
    }

    private val words = listOf("체리", "자두", "딸기", "오렌지", "사과", "키위", "메론", "포도", "복숭아", "레몬", "수박", "망고", "홍시", "자몽", "살구", "모과", "유자", "매실", "코코넛", "바나나", "석류", "대추", "두리안", "아보카도", "무화과", "블루베리", "파인애플", "한라봉", "감자", "고구마", "깻잎", "당근", "도라지", "마늘", "미나리", "버섯", "배추", "부추", "브로콜리", "생강", "시금치", "연근", "우엉", "양파", "호박", "옥수수", "청경채", "가지", "오이", "단무지", "피클", "상추", "바질", "인삼", "쑥갓", "피망", "고양이", "강아지", "거북이", "토끼", "사자", "호랑이", "표범", "치타", "하이에나", "기린", "코끼리", "코뿔소", "하마", "악어", "펭귄", "부엉이", "올빼미", "돼지", "독수리", "타조", "고릴라", "침팬지", "원숭이", "코알라", "캥거루", "고래", "상어", "칠면조", "청설모", "앵무새", "판다", "오소리", "오리", "거위", "백조", "두루미", "고슴도치", "두더지", "맹꽁이", "너구리", "개구리", "두꺼비", "카멜레온", "이구아나", "노루", "제비", "까치", "수달", "당나귀", "순록", "염소", "공작", "박쥐", "참새", "물개", "바다사자", "살모사", "구렁이", "얼룩말", "산양", "멧돼지", "도롱뇽", "북극곰", "미어캣", "비둘기", "돌고래", "까마귀", "낙타", "여우", "사슴", "늑대", "알파카", "다람쥐", "담비")
}
