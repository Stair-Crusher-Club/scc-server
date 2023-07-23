# 서버 배포

이 문서에서는 계단정복지도 서버의 DEV / PROD 환경 배포 방식에 대해 설명합니다. 인프라의 구조는 이 문서에서 설명하지 않습니다.

## CI / CD

scc-server 레포지토리는 github action을 기반으로 CI / CD를 실행합니다.

CI / CD 트리거는 각 환경별로 아래와 같습니다.
- DEV - main 브랜치에 대한 commit push
- PROD - semantic versioning을 따르는 이름의 git tag push (e.g. 0.1.0)

CI / CD가 트리거되면 DEV / PROD 환경 모두 동일하게 아래와 같은 로직이 수행됩니다.
1. scc-server 레포지토리를 빌드합니다. 이때 app-server 모듈, app-admin-frontend 모듈이 빌드가 되며, 빌드 과정에서 테스트도 실행됩니다.
2. 빌드된 이미지를 ECR에 푸시합니다.
3. 서버를 새로운 이미지로 rolling update 합니다.

더 자세한 내용은 [dev-ci-cd.yaml](/.github/workflows/dev-ci-cd.yaml) 파일과 [prod-ci-cd.yaml](/.github/workflows/prod-ci-cd.yaml) 파일을 참고해주세요.

## Config 관리

### app-server
app-server의 경우, 환경별로 달라져야 하는 값이 있을 때 크게 두 가지 방식을 사용할 수 있습니다.
- application.yaml - 공개되어도 괜찮은 값을 관리할 때 사용합니다.
- secret.yaml - 공개되어서는 안 되는 값을 관리할 때 사용합니다.

#### application.yaml
application.yaml의 경우, `infra/helm/scc-server/files/(dev|prod)/application.yaml` 파일로 관리합니다.

#### secret.yaml
secret.yaml의 경우, `infra/helm/scc-server/files/(dev|prod)/secret.yaml` 파일로 관리합니다.

secret.yaml의 암호화 / 복호화에는 [sops](https://github.com/getsops/sops)를 사용하고 있습니다. sops의 사용법은 아래와 같습니다.
1. AWS CLI 사용 환경을 설정합니다. [sops는 내부적으로 AWS KMS를 사용](https://github.com/getsops/sops#kms-trust-and-secrets-distribution)하므로, sops를 사용하기 위해서는 AWS KMS에 접근할 수 있는 권한을 가진 AWS 계정이 CLI 환경에 설정되어 있어야 합니다.
2. sops를 설치합니다.
3. `sops secret.yaml` 커맨드를 통해 secret.yaml 파일을 편집하고 커밋에 포함합니다.

이렇게 편집된 secret.yaml 파일은 서버 배포 시 자동으로 반영됩니다(secret 배포 -> 서버 배포 순으로 이루어집니다).

### app-admin-frontend
app-admin-frontend의 경우, [env-cmd](https://github.com/toddbluhm/env-cmd)를 활용하여 환경별로 달라져야 하는 값을 관리합니다.
`.env.(dev|prod)` 파일에 `REACT_APP_` prefix가 붙은 환경변수를 선언하면, 이를 ts 파일에서 `process.env.<REACT_APP_ prefix를 포함한 환경변수 이름>`와 같은 방식으로 사용할 수 있습니다.