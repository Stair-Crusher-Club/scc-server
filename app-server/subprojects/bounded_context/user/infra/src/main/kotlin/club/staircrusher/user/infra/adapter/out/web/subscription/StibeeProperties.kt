package club.staircrusher.user.infra.adapter.out.web.subscription

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.stibee")
data class StibeeProperties(
    /**
     * 스티비에서 생성한 API 키입니다.
     */
    val apiKey: String,
    /**
     * 주소록에 할당된 고유의 아이디입니다.
     * 주소록 목록에서 주소록 이름을 클릭하여 "주소록 대시보드"로 이동한 뒤, 브라우저에 표시되는 URL에서 확인할 수 있습니다.
     * ref: https://api.stibee.com/docs/
     */
    val listId: String,
)
