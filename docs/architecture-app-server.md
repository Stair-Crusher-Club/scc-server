# 계단정복지도 서버 프로젝트 코드 아키텍처

이 문서에서는 계단정복지도 서버 프로젝트의 코드 아키텍처에 대해 설명합니다.

이 문서를 통해 이루고자 하는 목적은 다음과 같습니다.
1. 개발자가 코드를 읽을 때 원하는 코드를 더욱 빠르게 찾도록 돕습니다. 이를 통해 신규 개발자의 빠른 적응을 돕고, 개발 속도를 향상시킵니다.
2. 개발자가 신규 기능을 개발할 때 아키텍처의 의도에 맞는 적절한 위치에 새로운 코드를 추가하도록 돕습니다. 이를 통해 코드베이스의 일관성을 유지합니다.

더 좋은 설계를 위한 제안은 issue나 PR을 활용해주세요.

## gradle modules

계단정복지도 서버 프로젝트는 강력한 의존성 관리를 위해 **코드의 논리적 단위를 패키지가 아닌 gradle module 단위로 분리**하고, 반드시 필요한 의존성만 gradle dependency로 선언합니다.

gradle module은 모두 `subprojects/` 디렉토리 아래에 존재하며, 크게 두 가지 종류의 module로 나뉩니다.
- [Bounded context module](/docs/architecture-app-server.md#bounded-context의-식별-및-분리) - 계단정복지도 서비스의 가장 핵심적인 도메인 로직과 유즈 케이스가 담긴 모듈입니다.
- [Cross-cutting concerns module](/docs/architecture-app-server.md#cross-cutting-concerns-modules) - bounded context에서 공통적으로 필요로 하는 cross-cutting concerns를 해결하기 위한 모듈입니다.

## DDD

계단정복지도 서버 프로젝트는 DDD를 통한 복잡도의 제어를 지향합니다.
이를 위해 bounded context의 식별 및 분리, hexagonal architecture, domain event를 적극적으로 사용하고 있습니다.

### Bounded Context의 식별 및 분리

2023년 6월 기준, 계단정복지도 서비스에는 크게 5가지의 bounded context(이하 BC)가 있습니다(alphabetical order). 
- accessibility - 접근성 정보 관리
- place - 장소 / 건물 정보 관리
- place_search - 장소 / 건물 검색 유즈 케이스 지원
- quest - 오프라인 클럽 활동을 위한 퀘스트 관리 
- user - 계정 정보 관리, 인증 처리

Bounded context의 경계는 프로젝트가 발전함에 따라 얼마든지 변경될 수 있습니다. e.g. 새로운 BC의 출현, 두 개 이상 BC의 통합, BC의 분리 등.

### Hexagonal architecture

각 BC는 hexagonal architecture를 따라 domain, application, infra 계층으로 나뉘어져 있습니다. 이들 계층은 각각 gradle module을 활용하여 격리됩니다.
- domain - 도메인의 핵심적인 규칙과 재사용성이 매우 높은 도메인 행동을 포함합니다.
- application - 애플리케이션이 외부와 소통하기 위한 port를 정의합니다.
  - `in` port - 애플리케이션 유즈 케이스 및 유즈 케이스를 지원하기 위한 재사용성이 낮은 동작들 
  - `out` port - persistence layer 접근 패턴(e.g. repository interface) 등
- infra - application 계층에서 정의된 port에 대한 adapter를 구현합니다.
  - `in` port - REST controller, domain event subscriber
  - `out` port - persistence layer 접근 상세 구현(e.g. RDB, in-memory DB) 등

### BC간의 의존 - 직접 호출의 경우

개발을 진행하다 보면 서로 다른 BC간에 의존이 필요한 경우가 있습니다. 이런 경우 크게 두 가지 방법이 있습니다.
1. 다른 BC의 로직을 직접 호출한다(e.g. 메소드 호출, REST API 호출).
2. 한 BC에서 domain event를 던지고, 다른 BC는 해당 이벤트를 구독해서 로직을 실행한다.

1번의 경우, 현재 계단정복지도 서비스는 monolith로 배포되므로 직접 메소드 호출을 해야 합니다. 이 경우, 빠른 개발 속돌르 위해 다른 BC의 application 계층에 직접 의존하고, 유즈케이스를 호출합니다.

이때 주의할 점으로, **다른 BC의 도메인 계층에는 절대 의존하지 않습니다**. 도메인 계층은 상대적으로 구현 상세에 가깝기 때문에, 해당 BC의 공식적인 인터페이스인 애플리케이션 계층에만 의존합니다.

### BC간의 의존 - Domain event 발행 / 구독

한편, **직접 다른 BC의 로직을 호출할 경우 점점 의존성 그래프가 복잡해지고 서비스간 결합도가 높아집니다**. 도메인적으로 연관성이 매우 높은 상황이 아니라면, **명시적인 의존을 최대한 피하는 것이 보다 확장성이 높은 설계입니다**.

장소 / 건물 정보가 등록된 직후, 우리는 정보 등록 랭킹을 갱신하고 싶을 수 있습니다. 이런 케이스가 한 개라면 직접 메소드 호출을 해도 괜찮을 것입니다. 하지만 계단정복지도 서비스가 (행복하게도) 매우 성공해서 장소 / 건물 정보가 등록된 이후 실행해야 하는 로직이 수십, 수백 개가 된다면 어떨까요? accessibility BC는 다른 수많은 BC를 알아야만 하고, 콜백 함수를 호출하는 코드만 수백, 수천 줄이 될 것입니다.

이러한 경우, domain event의 발행과 구독을 통해 의존성을 역전시켜 accessibility BC를 보호할 수 있습니다. accessibility BC는 `장소 / 건물 정보 등록 완료 이벤트`를 던지고, 다른 수십 / 수백 개의 BC에서 이를 구독하여 로직을 실행할 수 있습니다.

#### Domain event workflow

Domain event의 발행과 구독은 아래의 workflow로 작업할 수 있습니다.

1. Domain event 정의 - `domain_event` 모듈과 `domain_event_api` 모듈에서 domain event를 정의합니다. 정의하는 방식은 다른 domain event가 정의된 방식을 참고해 주세요.
2. Domain event 발행 - `stdlib` 모듈의 `DomainEventPublisher`를 사용해서 domain event를 발행할 수 있습니다.
3. Domain event 구독 - `stdlib` 모듈의 `DomainEventSubscriber`를 사용해서 domain event를 구독할 수 있습니다.

## Cross-cutting concerns modules

계단정복지도 서버 프로젝트는 다양한 cross-cutting concerns를 지원하기 위한 gradle module을 제공합니다(alphabetical order).
* packaging - 계단정복지도 서버 프로젝트의 코드를 하나로 묶어 배포 가능한 artifact를 만듭니다.
* spring_message - Domain event의 발행 / 구독 시스템을 실제로 구현합니다.
* spring_web - Spring MVC 환경에서 필요한 기능을 제공합니다. e.g. 보안 및 에러 핸들링에 필요한 util 등. 
* stdlib - BC를 개발할 때 공통적으로 필요한 기능을 제공합니다. 경계가 다소 모호한데, BC의 특정 계층이 아닌 전체 계층에서 사용 가능하면 보통 stdlib에 들어갑니다.
* testing - 테스트를 작성할 때 공통적으로 필요한 기능을 제공합니다. e.g. 테스트 데이터 생성, integration test 작성 템플릿 등.
