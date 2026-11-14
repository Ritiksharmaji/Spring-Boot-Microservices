# Observability

Three signals, all correlated by the **trace id**.

| Signal | Producer | Store | View |
|---|---|---|---|
| Metrics | Micrometer (Actuator `/actuator/prometheus`), tag `application=<service>` | Prometheus (7–15 days) | Grafana dashboard **E-Commerce Overview** |
| Traces | Micrometer Tracing + Brave, propagated over HTTP (W3C `traceparent`) and RabbitMQ headers | Zipkin | Zipkin UI, Grafana Zipkin datasource |
| Logs | Spring Boot console logs `… [traceId-spanId] …` | Loki (via Promtail, Docker service discovery) | Grafana Explore / dashboard logs panel; trace id links to Zipkin |
| Health | Actuator `health`, `liveness`, `readiness` | — | Docker health checks, Nginx, `/actuator/health` |

Start locally: `docker compose --profile observability up -d`, then Grafana http://localhost:3001
(admin/admin), Prometheus http://localhost:9090, Zipkin http://localhost:9411.

## Dashboard panels

| Panel | Query (PromQL) |
|---|---|
| Requests per second | `sum by (application)(rate(http_server_requests_seconds_count[1m]))` |
| 5xx per second | `… {status=~"5.."}` |
| Average latency | `rate(…_sum[1m]) / rate(…_count[1m])` |
| JVM heap | `sum by (application)(jvm_memory_used_bytes{area="heap"})` |
| Circuit breaker open | `resilience4j_circuitbreaker_state{state="open"}` |
| Breaker calls by outcome | `rate(resilience4j_circuitbreaker_calls_seconds_count[1m])` |
| Logs | Loki `{service=~"$application"}` |

## Useful queries

```promql
# p95 latency per endpoint of order-service
histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket{application="order-service"}[5m])))
# orders failing at checkout
sum(rate(http_server_requests_seconds_count{application="order-service", uri="/api/orders", method="POST", status!~"2.."}[5m]))
# RabbitMQ messages rejected by consumers (Spring Boot RabbitMQ metrics)
sum by (application) (rate(rabbitmq_rejected_total[5m]))
```

```logql
{service="order-service"} |= "ERROR"
{service=~"payment-service|order-service"} |= "order 42"
```

## Suggested alerts

| Alert | Condition |
|---|---|
| Service down | `up{job="spring-services"} == 0` for 2 m |
| High error rate | 5xx > 5 % of requests for 5 m |
| Circuit open | `resilience4j_circuitbreaker_state{state="open"} == 1` for 1 m |
| Outbox backlog | log/metric: unpublished outbox rows growing (broker down) |
| DLQ not empty | RabbitMQ: messages in any `*.dlq` queue |
| Memory pressure | heap used > 90 % of max for 10 m |

## Tracing an order end-to-end

1. Place an order; copy `traceId` from the order-service log line or Zipkin search `serviceName=order-service`.
2. Zipkin shows: gateway → order-service → product-service (GET + reserve) → user-service.
3. The asynchronous part (outbox → payment → order → notification) is linked through the
   message headers; search by the order id in Loki: `{service=~".+"} |= "order 42"`.
