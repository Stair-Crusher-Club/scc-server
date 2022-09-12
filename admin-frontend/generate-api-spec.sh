#!/bin/bash

# openapi-generator generate \
#   -i ../admin-api/api-spec.yaml \
#   --skip-validate-spec \
#   -g typescript-fetch \
#   --additional-properties=withInterfaces=true,typescriptThreePlus=true \
#   -o src/api
openapi-generator generate \
  -i ../admin-api/api-spec.yaml \
  --skip-validate-spec \
  -g typescript-axios \
  --additional-properties=withInterfaces=true,typescriptThreePlus=true \
  -o src/api
