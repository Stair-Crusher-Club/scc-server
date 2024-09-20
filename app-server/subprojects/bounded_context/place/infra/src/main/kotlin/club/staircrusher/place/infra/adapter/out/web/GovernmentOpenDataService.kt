package club.staircrusher.place.infra.adapter.out.web

import club.staircrusher.infra.network.createExternalApiService
import club.staircrusher.place.application.port.out.web.OpenDataService
import club.staircrusher.place.application.result.ClosedPlaceResult
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.CrsConverter
import club.staircrusher.stdlib.geography.CrsType
import club.staircrusher.stdlib.geography.Location
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import java.time.LocalDate

@Component
class GovernmentOpenDataService(
    private val properties: GovernmentOpenDataProperties,
) : OpenDataService {
    private val localDataService = createExternalApiService<LocalDataApiClient>(
        baseUrl = "http://www.localdata.go.kr/platform/rest/TO0",
        defaultHeadersBlock = {},
        defaultErrorHandler = { response ->
            response
                .bodyToMono(String::class.java)
                .map { RuntimeException(it) }
                .onErrorResume { response.createException() }
        },
    )

    override fun getClosedPlaces(): List<ClosedPlaceResult> {
        val restaurantResponse = localDataService.getClosedPlaceStatus(
            apiKey = properties.apiKey,
            opnSvcId = RESTAURANT_CODE,
        )
        val cafeResponse = localDataService.getClosedPlaceStatus(
            apiKey = properties.apiKey,
            opnSvcId = CAFE_CODE,
        )

        val closedRestaurants = restaurantResponse.result.body?.rows?.get(0)?.row?.mapNotNull {
            it.toDTO()
        } ?: emptyList()

        val closedCafe = cafeResponse.result.body?.rows?.get(0)?.row?.mapNotNull {
            it.toDTO()
        } ?: emptyList()

        return closedRestaurants + closedCafe
    }

    /**
     * https://www.localdata.go.kr/devcenter/apiGuide.do?menuNo=20002
     */
    private interface LocalDataApiClient {
        @GetExchange(
            url = "/openDataApi",
            accept = ["application/json"],
        )
        fun getClosedPlaceStatus(
            @RequestParam(name = "authKey") apiKey: String,
            @RequestParam(required = false) opnSvcId: String,
            @RequestParam(required = false) localCode: String = "6110000",
            @RequestParam(required = false) resultType: String = "json",
            @RequestParam(required = false) state: String = CLOSED_STATE,
            @RequestParam(required = false) pageSize: String = "50",
        ): GetPlaceStatusResponse

        data class GetPlaceStatusResponse(
            val result: Result,
        ) {
            data class Result(
                val header: Header?,
                val body: Body?,
            ) {
                data class Header(
                    val paging: Paging,
                    val process: Process,
                ) {
                    fun isSuccessful(): Boolean {
                        return process.code == "00"
                    }

                    data class Paging(
                        val pageIndex: Int,
                        val totalCount: Int,
                        val pageSize: Int,
                    )

                    data class Process(
                        val code: String,
                        val message: String,
                    )
                }

                data class Body(
                    val rows: List<Row> = emptyList(),
                ) {
                    data class Row(
                        val row: List<Data> = emptyList(),
                    ) {
                        data class Data(
                            // 개방자치단체코드 (PK1)
                            // pk 3개를 조합하면 unique 하다고 함
                            val opnSfTeamCode: String,

                            // 관리번호 (PK2)
                            val mgtNo: String,

                            // 개방서비스 ID (PK3)
                            val opnSvcId: String,

                            val rowNum: Int?,

                            // 데이터갱신일자
                            val updateDt: String?,

                            // 사업장명
                            val bplcNm: String?,

                            // 지번우편번호
                            val sitePostNo: String?,

                            // 지번주소
                            val siteWhlAddr: String?,

                            // 도로명우편번호
                            val rdnPostNo: String?,

                            // 도로명주소
                            val rdnWhlAddr: String?,

                            // 인허가일자
                            val apvPermYmd: String?,

                            // 인허가취소일자
                            val apvCancelYmd: String?,

                            // 폐업일자
                            val dcbYmd: String?,

                            // 휴업시작일자
                            val clgStdt: String?,

                            // 휴업종료일자
                            val clgEnddt: String?,

                            // 재개업일자
                            val ropnYmd: String?,

                            // 좌표정보(X)
                            val x: String?,

                            // 좌표정보(Y)
                            val y: String?,

                            // 최종수정일자
                            val lastModTs: String?,

                            // 업태구분명
                            val uptaeNm: String?,

                            // 전화번호
                            val siteTel: String?,
                        ) {
                            val location: Location?
                                get() = x?.let { locationConverter.toLocation(it.toDouble(), y!!.toDouble()) }
                        }
                    }
                }
            }
        }
    }

    private fun LocalDataApiClient.GetPlaceStatusResponse.Result.Body.Row.Data.toDTO(): ClosedPlaceResult? {
        return ClosedPlaceResult(
            name = bplcNm ?: return null,
            postalCode = rdnPostNo ?: return null,
            location = location ?: return null,
            phoneNumber = siteTel,
            closedDate = dcbYmd?.let { LocalDate.parse(it) } ?: return null,
        )
    }

    companion object {
        // https://www.localdata.go.kr/devcenter/apiGuide.do?menuNo=20002
        private const val CLOSED_STATE = "03"
        private const val RESTAURANT_CODE = "07_24_04_P"
        private const val CAFE_CODE = "07_24_05_P"

        private val locationConverter = CrsConverter(CrsType.EPSG_5174, CrsType.EPSG_4326)
    }
}
