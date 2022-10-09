package club.staircrusher.domain_event_api

import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.stairsrusher.domain_event_api.PlaceSearchEvent
import club.stairsrusher.domain_event_api.dto.Place
import com.squareup.wire.Message

@Component
class ProtoEventConverter {
    fun convertProtoToEvent(proto: Message<*, *>): DomainEvent {
       return when (proto) {
          is PlaceSearchEvent -> {
              club.staircrusher.domain_event.PlaceSearchEvent(
                  searchResult = proto.search_result.map(Place::toPlaceDTO)
              )
          }
           else -> {
               throw RuntimeException("can not handle this message type ${proto.javaClass}")
           }
       }
    }

    fun convertEventToProto(event: DomainEvent): Message<*, *> {
       return when (event) {
           is club.staircrusher.domain_event.PlaceSearchEvent -> {
               PlaceSearchEvent(search_result = event.searchResult.map(PlaceDTO::toPlace))
           }
           else -> {
               throw RuntimeException("can not handle this message type ${event.javaClass}")
           }
       }
    }
}