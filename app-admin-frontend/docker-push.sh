#!/bin/bash

if [[ $1 != "dev" && $1 != "prod" ]]; then
  echo 'Usage: ./docker-push.sh (dev|prod)'
  exit 0
fi;

IMAGE_TAG=latest
if [[ $1 = "dev" ]]; then
  IMAGE_TAG="$IMAGE_TAG-rc"
fi;

aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws/q0g6g7m8

npm install
npm run "build:$1"
docker buildx build --platform linux/amd64 -t public.ecr.aws/q0g6g7m8/scc-admin-frontend:$IMAGE_TAG .
docker push public.ecr.aws/q0g6g7m8/scc-admin-frontend:$IMAGE_TAG
