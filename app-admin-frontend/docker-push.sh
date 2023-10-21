#!/bin/bash

if [[ $1 != "dev" && $1 != "prod" ]]; then
  echo 'Usage: ./docker-push.sh (dev|prod) [<release-version>]; <release-version> is "latest" by default'
  exit 0
fi;

IMAGE_TAG="${2:-"latest"}"
if [[ $1 = "dev" ]]; then
  IMAGE_TAG="$IMAGE_TAG-rc"
fi;

aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws/q0g6g7m8

npm install -f # FIXME: 현재 사용하고 있는 blueprintjs 버전이 react 17 버전에 의존하는데, 실제로 사용하는 버전은 18이라서 -f가 없으면 오류가 난다.
npm run "build:$1"
docker buildx build --platform linux/amd64 -t public.ecr.aws/i6n1n6v2/scc-admin-frontend:$IMAGE_TAG .
docker push public.ecr.aws/i6n1n6v2/scc-admin-frontend:$IMAGE_TAG
