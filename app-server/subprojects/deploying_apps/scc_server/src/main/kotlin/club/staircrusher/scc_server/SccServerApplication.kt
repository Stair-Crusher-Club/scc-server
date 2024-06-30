package club.staircrusher.scc_server

import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType

@SpringBootApplication(
    proxyBeanMethods = false,
    exclude = [DataSourceTransactionManagerAutoConfiguration::class],
)
@ComponentScan(
    basePackages = ["club.staircrusher"],
    includeFilters = [
        Filter(type = FilterType.ANNOTATION, classes = [Component::class]),
    ],
)
@ConfigurationPropertiesScan(
    basePackages = ["club.staircrusher"],
)
open class SccServerApplication {
    private val logger = KotlinLogging.logger {  }
    @Bean
    fun hi(i: ClubQuestTargetPlacesSearcher) = ApplicationRunner {

        CoroutineScope(Dispatchers.IO).launch {
            logger.info {"start serach places"}
            val places = i.searchPlaces(
                centerLocation = Location(lat=37.54638471729234, lng=127.05375274602797),
                radiusMeters = 300,
            )
            places.forEach {
                println(
                    "${it.name}\t${it.building.address}"
                )
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<SccServerApplication>(*args)
}
