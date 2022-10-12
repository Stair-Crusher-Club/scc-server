package club.staircrusher.domain_event_api

import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.stairsrusher.domain_event_api.proto.Place
import club.stairsrusher.domain_event_api.proto.event.PlaceSearchEvent
import com.squareup.wire.Message

@Component
class ProtoEventConverter {
    fun convertProtoToEvent(proto: Message<*, *>): DomainEvent {
       return when (proto) {
          is PlaceSearchEvent -> {
              club.staircrusher.domain_event.PlaceSearchEvent(
                  searchResult = proto.search_result.map(Place::toDTO)
              )
          }
           else -> {
               throw error("can not handle this message type ${proto.javaClass}")
           }
       }
    }

    fun convertEventToProto(event: DomainEvent): Message<*, *> {
       return when (event) {
           is club.staircrusher.domain_event.PlaceSearchEvent -> {
               PlaceSearchEvent(search_result = event.searchResult.map(PlaceDTO::toProto))
           }
           else -> {
               throw error("can not handle this message type ${event.javaClass}")
           }
       }
    }
}
