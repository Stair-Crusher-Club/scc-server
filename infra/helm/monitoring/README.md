# 모니터링 Chart 배포

secret 을 helm value 혹은 ENV 로 주입하여 배포되도록 하려고 했으나 마땅한 방법이 없어서 일단 --set 옵션으로 배포합니다.

```bash
helm upgrade --install monitoring ./ \
  --namespace monitoring \
  -f values.yaml \
  --set openobserve-collector.exporters."otlphttp/openobserve".endpoint={secret.yaml 참조}  \
  --set openobserve-collector.exporters."otlphttp/openobserve".headers.Authorization="{secret.yaml 참조}"  \
  --set openobserve-collector.exporters."otlphttp/openobserve_k8s_events".endpoint={secret.yaml 참조}  \
  --set openobserve-collector.exporters."otlphttp/openobserve_k8s_events".headers.Authorization="{secret.yaml 참조}"
```