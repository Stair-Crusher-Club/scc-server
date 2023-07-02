#!/bin/bash

# openapi-generator generate \
#   -i ../api-admin/api-spec.yaml \
#   --skip-validate-spec \
#   -g typescript-fetch \
#   --additional-properties=withInterfaces=true,typescriptThreePlus=true \
#   -o src/api
openapi-generator generate \
  -i ../api-admin/api-spec.yaml \
  --skip-validate-spec \
  -g typescript-axios \
  --additional-properties=withInterfaces=true,typescriptThreePlus=true \
  -o src/api
