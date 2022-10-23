#!/bin/bash

openssl s_client -showcerts -verify 5 -connect $1:443 < /dev/null |
  awk '/BEGIN CERTIFICATE/,/END CERTIFICATE/{ if(/BEGIN CERTIFICATE/){a++}; out="cert"a".pem"; print >out}'

for cert in *.pem; do
  THUMBPRINT=$(openssl x509 -fingerprint -noout -in $cert | sed 's/://g' | awk -F= '{print tolower($2)}')
  THUMBPRINT_JSON="{\"thumbprint\": \"${THUMBPRINT}\"}"
done

rm -rf *.pem

echo $THUMBPRINT_JSON
