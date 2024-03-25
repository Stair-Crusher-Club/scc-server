# 모니터링 Chart 배포

## 필요한 CRD 설치
```bash
kubectl apply -f manifests/crd-podmonitors.yaml
kubectl apply -f manifests/crd-servicemonitors.yaml
```
[이 PR](https://github.com/openobserve/openobserve/pull/2884)에서 고쳐졌지만 아직 Helm chart 에 적용되진 않아서 직접 설치합니다


## Helm Chart 설치

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