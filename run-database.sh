#!/bin/bash

POSTGRES_IMAGE_VERSION=14.5-alpine

docker pull postgres:$POSTGRES_IMAGE_VERSION

docker run \
  --name scc-postgres \
  -p 15432:5432 \
  -e POSTGRES_USER=test \
  -e POSTGRES_PASSWORD=test \
  -e POSTGRES_DB=scc \
  -d postgres:14.5-alpine
