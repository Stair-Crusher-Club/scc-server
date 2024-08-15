# 코드베이스 아키텍처

## 개요

scc-server 레포지토리는 크게 아래와 같이 구성되어 있습니다.
```
scc-backend/
├─ api-admin/               - 어드민 api spec
├─ app-server/              - scc 통합 API 서버 코드
└─ infra/                   - scc 통합 인프라 코드
```

## app-server

app-server는 계단정복지도 서비스의 통합 API 서버 프로젝트입니다.
구체적으로, 계단정복지도 앱과 계단정복지도 어드민을 위한 API를 서빙합니다.

## api-admin
계단정복지도 어드민의 OpenAPI 기반 api spec을 정의하는 프로젝트입니다.

## infra

계단정복지도 백엔드의 인프라를 구성하는 프로젝트입니다.
구체적으로, 인프라 provisioning을 위한 terraform과, k8s 위에 서버를 배포하기 위한 helm chart로 구성되어 있습니다.
