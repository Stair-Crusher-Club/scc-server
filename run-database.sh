#!/bin/bash

POSTGRES_IMAGE_VERSION=14.5-alpine

docker pull postgres:$POSTGRES_IMAGE_VERSION

docker run --name scc-postgres -e POSTGRES_USER=test -e POSTGRES_PASSWORD=test -p 15432:5432 -d postgres:14.5-alpine
