package club.staircrusher.external_accessibility.infra.adapter.out.web

import club.staircrusher.external_accessibility.application.port.out.web.ToiletInfoFetcher
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.util.string.emptyToNull
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.nio.charset.Charset

@Component
class ToiletInfoFetcherImpl : ToiletInfoFetcher {
    override fun fetchRecords(): List<ToiletInfoFetcher.ToiletRow> {
        val client = WebClient.builder()
            .codecs { it.defaultCodecs().maxInMemorySize(4 * 1024 * 1024) }
            .build()
        return client.get()
            .uri(CSV_BASE_URL)
            .accept(MediaType.valueOf("text/csv"))
            .exchangeToMono { response ->
                if (response.statusCode().is2xxSuccessful) {
                    DataBufferUtils.join(response.bodyToFlux(DataBuffer::class.java))
                } else {
                    Mono.error(RuntimeException("Failed to fetch ${response.statusCode()}"))
                }
            }
            .map { dataBuffer ->
                val parser = CSVParser.parse(
                    dataBuffer.asInputStream(),
                    Charset.forName("UTF-8"),
                    CSVFormat.DEFAULT.withFirstRecordAsHeader()
                )
                parser.stream()
                    .map {
                        ToiletInfoFetcher.ToiletRow(
                            it.get("cot_conts_id").emptyToNull()!!,
                            it.get("cot_conts_name").emptyToNull()!!,
                            IMAGE_BASE_URL + (it.get("cot_img_main_url").emptyToNull() ?: return@map null),
                            it.get("cot_addr_full_old").emptyToNull() ?: return@map null,
                            it.get("cot_addr_full_new").emptyToNull() ?: return@map null,
                            it.get("cot_value_01").emptyToNull(),
                            it.get("cot_value_02").emptyToNull(),
                            it.get("cot_value_03").emptyToNull(),
                            it.get("cot_value_04").emptyToNull(),
                            it.get("cot_value_05").emptyToNull(),
                            it.get("cot_value_06").emptyToNull(),
                            it.get("cot_value_07").emptyToNull(),
                            it.get("cot_value_08").emptyToNull(),
                            it.get("cot_value_09").emptyToNull(),
                            it.get("cot_value_10").emptyToNull(),
                            it.get("cot_value_11").emptyToNull(),
                            it.get("cot_value_12").emptyToNull(),
                            it.get("lat").toDoubleOrNull() ?: return@map null,
                            it.get("lng").toDoubleOrNull() ?: return@map null,

                        )
                    }
                    .toList()
                    .mapNotNull { it }
            }
            .block() ?: emptyList()
    }

    companion object {
        const val CSV_BASE_URL = "https://bucket-cxcjy2.s3.ap-northeast-2.amazonaws.com/toilet_info_with_latlng.csv"
        const val IMAGE_BASE_URL = "https://map.seoul.go.kr"
    }
}
