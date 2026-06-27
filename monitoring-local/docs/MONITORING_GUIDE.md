# Monitoring Guide


이 문서는 `module01-actuator` 프로젝트에서 사용하는 모니터링 기술을 기준별로 정리한 가이드입니다.


수업에서 배운 Actuator, Micrometer, Prometheus, Grafana를 현재 코드와 연결해서 설명합니다.


## 1. 한 줄 요약


이 프로젝트의 모니터링 흐름은 다음과 같습니다.


```text

Spring Boot App

  -> Actuator가 운영용 endpoint를 열어준다

  -> Micrometer가 metric을 기록한다

  -> Prometheus가 /actuator/prometheus에서 metric을 수집한다

  -> Grafana가 Prometheus 데이터를 그래프로 보여준다

```


쉽게 말하면 다음과 같습니다.


| 기술 | 쉬운 비유 | 역할 |

|---|---|---|

| Actuator | 가게 관리실 창문 | 앱 상태를 HTTP로 보여준다 |

| Micrometer | 숫자를 재는 계기판 | 주문 수, 오류 수, 처리 시간을 기록한다 |

| MeterRegistry | 계기판을 모아두는 장부 | Counter, Timer 같은 metric을 등록한다 |

| Prometheus | 숫자를 계속 모으는 기록 창고 | 시간에 따른 metric을 저장한다 |

| Grafana | 기록을 예쁜 그래프로 보여주는 TV | 대시보드와 알림을 만든다 |

| HealthIndicator | 건강검진 항목 | DB, 외부 API 같은 구성 요소 상태를 확인한다 |

| Log | 사건 일기장 | 특정 요청에서 무슨 일이 있었는지 자세히 남긴다 |


## 2. 현재 프로젝트에 들어간 모니터링 설정


### build.gradle


`build.gradle`에는 Actuator와 Prometheus 구현체가 들어가 있습니다.


```groovy

implementation 'org.springframework.boot:spring-boot-starter-actuator'

runtimeOnly 'io.micrometer:micrometer-registry-prometheus'

```


의미는 다음과 같습니다.


| 의존성 | 의미 |

|---|---|

| `spring-boot-starter-actuator` | `/actuator/health`, `/actuator/metrics` 같은 운영 endpoint를 만든다 |

| `micrometer-registry-prometheus` | Micrometer metric을 Prometheus가 읽을 수 있는 형식으로 바꾼다 |


### application.yaml


`application.yaml`에는 외부로 공개할 Actuator endpoint가 설정되어 있습니다.


```yaml

management:

  endpoints:

    web:

      exposure:

        include: health,info,metrics,shop,prometheus

```


현재 공개된 endpoint는 다음과 같습니다.


| endpoint | 목적 |

|---|---|

| `/actuator/health` | 앱이 살아 있는지 확인 |

| `/actuator/info` | 앱 정보 확인 |

| `/actuator/metrics` | Actuator 방식으로 metric 확인 |

| `/actuator/shop` | 직접 만든 쇼핑몰 운영 요약 확인 |

| `/actuator/prometheus` | Prometheus가 수집할 metric 확인 |


주의할 점은 `exposure.include`가 보안을 적용하는 설정은 아니라는 점입니다.

현재 프로젝트에는 Spring Security 설정이 없으므로 운영 환경에서는 접근 제어가 필요합니다.


## 3. 기술별 사용 기준


어떤 상황에 어떤 모니터링 기술을 써야 하는지 기준은 다음과 같습니다.


| 상황 | 써야 하는 기술 | 이유 |

|---|---|---|

| 앱이 살아 있는지만 알고 싶다 | Actuator Health | 가장 단순한 생존 확인이다 |

| DB, 외부 API 상태를 확인하고 싶다 | HealthIndicator | 구성 요소별 정상 여부를 확인할 수 있다 |

| 현재 상품 수, 주문 수를 즉시 보고 싶다 | Custom Actuator Endpoint | 지금 시점의 요약 정보를 바로 보여준다 |

| 주문 성공 수를 세고 싶다 | Micrometer Counter | 계속 증가하는 사건 수에 적합하다 |

| 주문 금액 분포를 보고 싶다 | DistributionSummary | 값의 크기와 분포를 기록할 수 있다 |

| 결제 처리 시간을 보고 싶다 | Timer | 실행 횟수와 실행 시간을 함께 기록한다 |

| metric을 오래 저장하고 싶다 | Prometheus | 앱이 재시작되어도 이전 기록을 볼 수 있다 |

| metric을 그래프로 보고 싶다 | Grafana | Prometheus 데이터를 대시보드로 보여준다 |

| 문제가 생겼을 때 원인을 자세히 보고 싶다 | Log | 특정 사건의 상세 흐름을 볼 수 있다 |


## 4. Actuator를 써야 하는 기준


Actuator는 애플리케이션의 운영 정보를 HTTP로 확인할 때 사용합니다.


이 프로젝트에서는 다음 상황에 필요합니다.


### 앱 생존 확인


```http

GET /actuator/health

```


서버가 정상이라면 다음과 같은 응답을 받습니다.


```json

{

  "status": "UP"

}

```


이 endpoint는 다음 곳에서 자주 사용합니다.


- 로드 밸런서가 서버를 트래픽 대상에 넣을지 판단할 때

- Kubernetes가 Pod를 재시작할지 판단할 때

- 운영자가 서버 상태를 빠르게 확인할 때


### 개발 중 metric 확인


```http

GET /actuator/metrics

GET /actuator/metrics/shop.orders.created

```


Actuator의 `/metrics`는 개발자가 metric이 제대로 찍히는지 확인할 때 좋습니다.


하지만 장기 저장이나 그래프 분석에는 Prometheus와 Grafana를 사용해야 합니다.


### 직접 만든 운영 요약 확인


```http

GET /actuator/shop

```


`ShopEndPoint`가 만든 custom endpoint입니다.


```json

{

  "step": "STEP 1",

  "productCount": 7,

  "orderCount": 6

}

```


이 endpoint는 시간 흐름을 저장하지 않습니다.

그냥 지금 DB 상태를 바로 조회합니다.


따라서 다음 기준으로 사용합니다.


| 필요한 것 | 적합 여부 |

|---|---|

| 현재 상품 수 확인 | 적합 |

| 현재 주문 수 확인 | 적합 |

| 시간별 주문 수 그래프 | 부적합 |

| 알림 조건 만들기 | 부적합 |


## 5. Micrometer를 써야 하는 기준


Micrometer는 코드 안에서 의미 있는 숫자를 직접 기록할 때 사용합니다.


이 프로젝트의 핵심 클래스는 `ShopMetrics`입니다.


```text

src/main/java/com/wanted/actuator/metric/ShopMetrics.java

```


### Counter 기준


Counter는 사건이 몇 번 발생했는지 셀 때 사용합니다.


Counter를 써야 하는 상황:


- 주문이 성공한 횟수

- 주문이 실패한 횟수

- API 오류가 발생한 횟수

- 인기 상품 API가 호출된 횟수


현재 코드의 Counter는 다음과 같습니다.


| metric | 기록 위치 | 의미 |

|---|---|---|

| `shop.orders.created` | `OrderService.createOrder()` | 주문 성공 횟수 |

| `shop.orders.failed` | `OrderService.createOrder()` catch | 주문 실패 횟수 |

| `shop.api.errors` | `ApiExceptionHandler` | 사용자에게 오류 응답을 준 횟수 |

| `shop.products.popular.requests` | `ProductService.getPopularProducts()` | 인기 상품 API 호출 횟수 |


Counter를 쓰면 안 좋은 상황:


- 현재 재고 수처럼 증가와 감소가 모두 필요한 값

- 현재 접속자 수처럼 오르내리는 값


그런 값은 Gauge가 더 적합합니다.


### Timer 기준


Timer는 어떤 작업이 얼마나 오래 걸리는지 알고 싶을 때 사용합니다.


Timer를 써야 하는 상황:


- 주문 생성 시간이 얼마나 걸리는가

- 결제 API 호출이 얼마나 느린가

- 인기 상품 집계 쿼리가 얼마나 오래 걸리는가

- HTTP 요청이 얼마나 오래 걸리는가


현재 코드의 Timer는 다음과 같습니다.


| metric | 기록 위치 | 의미 |

|---|---|---|

| `shop.orders.creation.duration` | `OrderService.createOrder()` | 주문 전체 처리 시간 |

| `shop.payment.duration` | `PaymentService.pay()` | 결제 처리 시간 |

| `shop.products.popular.query.duration` | `ProductService.getPopularProducts()` | 인기 상품 쿼리 시간 |


Timer는 보통 다음 질문에 답합니다.


```text

이 기능은 몇 번 실행됐나?

전체 시간은 얼마인가?

평균 시간은 얼마인가?

가장 오래 걸린 시간은 얼마인가?

```


### DistributionSummary 기준


DistributionSummary는 시간이 아닌 숫자 값의 분포를 기록할 때 사용합니다.


현재 코드의 DistributionSummary는 다음과 같습니다.


| metric | 기록 위치 | 의미 |

|---|---|---|

| `shop.orders.item.count` | 주문 성공 시 | 한 주문에 포함된 상품 종류 수 |

| `shop.orders.amount` | 주문 성공 시 | 주문 금액 |


DistributionSummary를 써야 하는 상황:


- 주문 금액이 얼마나 큰가

- 한 주문에 상품이 몇 종류 들어가는가

- 파일 업로드 크기가 얼마나 되는가

- 장바구니 상품 개수가 얼마나 되는가


Timer와 헷갈리면 다음 기준으로 구분합니다.


| 질문 | 사용 기술 |

|---|---|

| 얼마나 오래 걸렸나? | Timer |

| 값이 얼마나 큰가? | DistributionSummary |


## 6. Prometheus를 써야 하는 기준


Prometheus는 metric을 시간 순서로 저장할 때 사용합니다.


Actuator의 `/actuator/metrics`는 지금 앱이 알고 있는 값을 보여줍니다.

하지만 앱이 재시작되면 메모리에 있던 metric은 사라집니다.


Prometheus는 주기적으로 다음 주소를 호출해서 metric을 가져갑니다.


```http

GET /actuator/prometheus

```


Prometheus가 필요한 기준은 다음과 같습니다.


| 상황 | Prometheus 필요 여부 |

|---|---|

| 개발자가 지금 값만 확인 | 낮음 |

| 1시간 전 주문 수와 지금 주문 수 비교 | 높음 |

| 서버 재시작 후에도 과거 metric 확인 | 높음 |

| 여러 서버 인스턴스의 metric 합산 | 높음 |

| 특정 조건에서 알림 발생 | 높음 |


Prometheus에서는 Micrometer metric 이름이 Prometheus 스타일로 변환됩니다.


예시는 다음과 같습니다.


| Micrometer 이름 | Prometheus에서 보이는 형태 |

|---|---|

| `shop.orders.created` | `shop_orders_created_total` |

| `shop.orders.failed` | `shop_orders_failed_total{reason="invalid_request"}` |

| `shop.api.errors` | `shop_api_errors_total{reason="bad_request"}` |

| `shop.payment.duration` | `shop_payment_duration_seconds_count`, `shop_payment_duration_seconds_sum`, `shop_payment_duration_seconds_max` |


정확한 이름은 `/actuator/prometheus`에서 직접 확인하는 것이 가장 안전합니다.


### Prometheus scrape 설정 예시


Prometheus는 보통 다음처럼 앱을 주기적으로 긁어갑니다.


```yaml

scrape_configs:

  - job_name: "popular-shop"

    metrics_path: "/actuator/prometheus"

    static_configs:

      - targets: ["localhost:8080"]

```


여기서 중요한 기준은 다음과 같습니다.


| 설정 | 기준 |

|---|---|

| `job_name` | 어떤 앱에서 온 metric인지 구분하는 이름 |

| `metrics_path` | Spring Boot에서는 보통 `/actuator/prometheus` |

| `targets` | 수집 대상 서버 주소 |

| scrape interval | 보통 15초 또는 30초부터 시작 |


## 7. Grafana를 써야 하는 기준


Grafana는 Prometheus에 저장된 숫자를 사람이 보기 좋은 그래프로 보여줍니다.


Grafana를 써야 하는 기준은 다음과 같습니다.


| 상황 | Grafana 필요 여부 |

|---|---|

| 개발자가 metric 이름만 확인 | 낮음 |

| 운영자가 실시간 상황을 한 화면에서 확인 | 높음 |

| 장애 당시 그래프를 보고 원인 추적 | 높음 |

| 팀원과 지표를 공유 | 높음 |

| 알림 기준을 시각적으로 관리 | 높음 |


이 프로젝트에서 Grafana 대시보드를 만든다면 다음 패널을 추천합니다.


### 비즈니스 대시보드


| 패널 | Prometheus 기준 |

|---|---|

| 초당 주문 성공 수 | `rate(shop_orders_created_total[5m])` |

| 초당 주문 실패 수 | `rate(shop_orders_failed_total[5m])` |

| 주문 실패 사유별 비율 | `sum by (reason) (rate(shop_orders_failed_total[5m]))` |

| API 오류 사유별 비율 | `sum by (reason) (rate(shop_api_errors_total[5m]))` |

| 평균 주문 금액 | `rate(shop_orders_amount_sum[5m]) / rate(shop_orders_amount_count[5m])` |

| 인기 상품 API 호출량 | `rate(shop_products_popular_requests_total[5m])` |


### 지연 시간 대시보드


| 패널 | Prometheus 기준 |

|---|---|

| 평균 주문 생성 시간 | `rate(shop_orders_creation_duration_seconds_sum[5m]) / rate(shop_orders_creation_duration_seconds_count[5m])` |

| 평균 결제 시간 | `rate(shop_payment_duration_seconds_sum[5m]) / rate(shop_payment_duration_seconds_count[5m])` |

| 평균 인기 상품 쿼리 시간 | `rate(shop_products_popular_query_duration_seconds_sum[5m]) / rate(shop_products_popular_query_duration_seconds_count[5m])` |

| HTTP 요청 평균 시간 | `rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])` |


### 시스템 대시보드


| 패널 | 예시 metric |

|---|---|

| JVM Heap 메모리 사용량 | `jvm_memory_used_bytes` |

| GC 발생 시간 | `jvm_gc_pause_seconds_*` |

| CPU 사용량 | `process_cpu_usage`, `system_cpu_usage` |

| Thread 수 | `jvm_threads_live_threads` |

| DB Connection Pool | `hikaricp_connections_*` |

| Prometheus scrape 성공 여부 | `up{job="popular-shop"}` |


## 8. 이 프로젝트 파일별 모니터링 기준


### OrderService


파일:


```text

src/main/java/com/wanted/actuator/order/OrderService.java

```


여기서는 주문 생성이라는 핵심 비즈니스가 실행됩니다.


필요한 모니터링:


| 관찰 대상 | 사용 기술 | 이유 |

|---|---|---|

| 주문 성공 수 | Counter | 주문이 실제로 얼마나 만들어지는지 확인 |

| 주문 실패 수 | Counter + reason tag | 재고 부족, 잘못된 요청, 서버 오류를 구분 |

| 주문 처리 시간 | Timer | 주문이 느려졌는지 확인 |

| 주문 금액 | DistributionSummary | 주문 규모가 커지는지 확인 |

| 주문 상품 종류 수 | DistributionSummary | 주문 패턴을 확인 |


이 파일은 비즈니스의 중심이므로 custom metric을 넣는 것이 적절합니다.


### PaymentService


파일:


```text

src/main/java/com/wanted/actuator/payment/PaymentService.java

```


결제는 외부 시스템과 연결되는 지점이라고 생각해야 합니다.


필요한 모니터링:


| 관찰 대상 | 사용 기술 | 이유 |

|---|---|---|

| 결제 처리 시간 | Timer | 외부 결제가 느려졌는지 확인 |

| 결제 실패 수 | Counter | 실제 결제 API가 있다면 실패율 확인 필요 |

| 결제 시스템 건강 상태 | HealthIndicator | 외부 결제 서버 상태 확인 |


현재 코드는 `Thread.sleep(500)`으로 결제 지연을 흉내 냅니다.

실제 서비스에서는 외부 결제 API 호출 결과와 실패 원인을 별도 metric으로 기록하는 것이 좋습니다.


### ProductService


파일:


```text

src/main/java/com/wanted/actuator/product/ProductService.java

```


인기 상품 조회는 DB 집계 쿼리를 실행합니다.


필요한 모니터링:


| 관찰 대상 | 사용 기술 | 이유 |

|---|---|---|

| 인기 상품 API 호출 수 | Counter | 자주 호출되는 기능인지 확인 |

| 인기 상품 쿼리 시간 | Timer | DB 집계 쿼리가 느려지는지 확인 |

| DB 부하 | Prometheus + Grafana | 호출량 증가와 쿼리 지연을 함께 분석 |


이 API는 화면에서 자주 호출될 가능성이 높으므로 latency를 반드시 봐야 합니다.


### ApiExceptionHandler


파일:


```text

src/main/java/com/wanted/actuator/global/ApiExceptionHandler.java

```


사용자에게 오류 응답을 줄 때마다 metric을 기록합니다.


필요한 모니터링:


| 관찰 대상 | 사용 기술 | 이유 |

|---|---|---|

| 잘못된 요청 수 | Counter + `bad_request` tag | 클라이언트 요청 문제 확인 |

| 검증 실패 수 | Counter + `validation` tag | 입력값 검증 문제 확인 |

| 서버 오류 수 | Counter + `server_error` tag | 장애 가능성 확인 |


여기서 좋은 점은 예외 메시지를 tag로 쓰지 않는다는 점입니다.

tag 값은 반드시 제한된 개수만 사용해야 합니다.


좋은 tag:


```text

reason="bad_request"

reason="validation"

reason="server_error"

```


나쁜 tag:


```text

reason="상품 1번 재고 부족"

reason="상품 2번 재고 부족"

reason="userId=123 요청 실패"

```


tag가 너무 많아지면 Prometheus 성능에 문제가 생깁니다.


### ShopEndPoint


파일:


```text

src/main/java/com/wanted/actuator/metric/ShopEndPoint.java

```


이 파일은 metric을 저장하는 것이 아니라 현재 상태를 바로 보여줍니다.


필요한 기준:


| 목적 | 적합 여부 |

|---|---|

| 현재 상품 수 확인 | 적합 |

| 현재 주문 수 확인 | 적합 |

| 시간별 주문 추이 확인 | 부적합 |

| Grafana 그래프 생성 | 부적합 |


시간에 따른 변화를 보고 싶다면 `ShopEndPoint`가 아니라 Prometheus metric을 사용해야 합니다.


## 9. 모니터링 설계 기준


새 기능을 만들 때는 다음 순서로 질문하면 됩니다.


### 1단계: 이것은 현재 상태인가?


예:


- 앱이 살아 있는가?

- DB가 연결되어 있는가?

- 현재 상품 수가 몇 개인가?


선택:


```text

Actuator Health

Custom Actuator Endpoint

HealthIndicator

```


### 2단계: 이것은 사건 횟수인가?


예:


- 주문이 몇 번 성공했는가?

- 주문이 몇 번 실패했는가?

- API 오류가 몇 번 발생했는가?


선택:


```text

Micrometer Counter

Prometheus rate()

Grafana graph

```


### 3단계: 이것은 처리 시간인가?


예:


- 주문 생성이 얼마나 걸렸는가?

- 결제가 얼마나 걸렸는가?

- DB 쿼리가 얼마나 걸렸는가?


선택:


```text

Micrometer Timer

Prometheus average query

Grafana latency panel

```


### 4단계: 이것은 값의 크기인가?


예:


- 주문 금액이 얼마인가?

- 주문에 상품 종류가 몇 개인가?

- 업로드 파일 크기가 얼마인가?


선택:


```text

DistributionSummary

Grafana distribution or average panel

```


### 5단계: 이것은 자세한 원인 분석인가?


예:


- 어떤 요청에서 오류가 났는가?

- 어떤 상품 ID에서 문제가 났는가?

- 어떤 예외 stack trace가 발생했는가?


선택:


```text

Log

Trace

Error report

```


metric tag에 userId, orderId, exception message를 넣지 않습니다.


## 10. 알림 기준 예시


운영 환경에서는 Grafana Alert 또는 Prometheus Alertmanager를 사용해 알림을 만들 수 있습니다.


이 프로젝트 기준 예시는 다음과 같습니다.


| 알림 상황 | 기준 예시 |

|---|---|

| 서버가 죽음 | `up{job="popular-shop"} == 0` |

| 주문 실패 급증 | `rate(shop_orders_failed_total[5m]) > 1` |

| API 서버 오류 발생 | `rate(shop_api_errors_total{reason="server_error"}[5m]) > 0` |

| 평균 결제 지연 증가 | `rate(shop_payment_duration_seconds_sum[5m]) / rate(shop_payment_duration_seconds_count[5m]) > 1` |

| 평균 주문 처리 지연 증가 | `rate(shop_orders_creation_duration_seconds_sum[5m]) / rate(shop_orders_creation_duration_seconds_count[5m]) > 2` |

| 인기 상품 쿼리 지연 증가 | `rate(shop_products_popular_query_duration_seconds_sum[5m]) / rate(shop_products_popular_query_duration_seconds_count[5m]) > 0.5` |

| JVM 메모리 사용량 높음 | heap 사용률 80% 이상 |

| DB 커넥션 부족 | Hikari active connection이 max에 가까움 |


수업 프로젝트에서는 임계값을 낮게 잡아도 되지만, 실제 운영에서는 평소 트래픽 기준을 보고 조정해야 합니다.


## 11. 현재 코드에서 개선하면 좋은 부분


### 1. 재고 부족 예외와 metric reason 불일치


현재 `ShopMetrics.failureReason()`에는 다음 분기가 있습니다.


```java

if (exception instanceof InsufficientStockException) {

    return "insufficient_stock";

}

```


하지만 실제 `Product.decreaseStock()`은 `InsufficientStockException`이 아니라 `IllegalArgumentException`을 던집니다.


그래서 재고 부족도 `invalid_request`로 기록됩니다.


재고 부족을 따로 보고 싶다면 `Product.decreaseStock()`에서 `InsufficientStockException`을 던지도록 바꾸는 것이 좋습니다.


### 2. HealthIndicator 추가


수업 내용에는 custom HealthIndicator가 있었지만 현재 프로젝트에는 없습니다.


추가하면 좋은 HealthIndicator:


| 대상 | 이유 |

|---|---|

| 결제 API | 외부 결제 서버가 죽으면 주문 생성이 실패하기 때문 |

| MySQL | DB 연결 상태가 서비스 핵심이기 때문 |

| 인기 상품 쿼리 의존 DB | 집계 쿼리가 핵심 화면과 연결될 수 있기 때문 |


### 3. `/actuator/info` 정보 추가


현재 `/actuator/info`는 공개되어 있지만 보여줄 정보가 없습니다.


예시:


```yaml

info:

  app:

    name: popular-shop

    version: 1.0.0

```


배포 버전, 빌드 시간, git commit hash를 넣으면 운영 중인 버전을 확인하기 좋습니다.


### 4. 운영 보안 설정


현재는 Actuator endpoint가 공개되어 있습니다.


운영 기준:


| endpoint | 공개 기준 |

|---|---|

| `/actuator/health` | 제한적으로 공개 가능 |

| `/actuator/prometheus` | Prometheus 서버만 접근 허용 |

| `/actuator/metrics` | 외부 공개 금지 |

| `/actuator/shop` | 외부 공개 금지 |

| `/actuator/info` | 민감 정보가 없을 때만 제한 공개 |


## 12. 최종 판단 기준


새로운 기능에 모니터링을 붙일 때는 아래 기준을 사용합니다.


```text

현재 정상인지 알고 싶다

  -> health 또는 custom endpoint


몇 번 일어났는지 알고 싶다

  -> Counter


얼마나 오래 걸렸는지 알고 싶다

  -> Timer


값이 얼마나 큰지 알고 싶다

  -> DistributionSummary


시간 흐름으로 저장하고 싶다

  -> Prometheus


그래프로 보고 싶다

  -> Grafana


장애 때 자세한 원인을 보고 싶다

  -> Log 또는 Trace

```


서비스 대시보드를 만들 때는 보통 다음 4가지를 기본 기준으로 삼습니다.


1. 핵심 비즈니스 이벤트가 정상적으로 발생하는가?

2. 실패나 오류가 평소보다 늘고 있는가?

3. 사용자가 기다리는 핵심 처리 구간이 느려지고 있는가?

4. 런타임, DB, HTTP 요청 상태가 안정적인가?


도메인마다 이름은 달라질 수 있습니다. 예를 들어 쇼핑 서비스라면 주문과 결제가 핵심 이벤트가 되고, 게시판 서비스라면 글 작성과 댓글 작성이 핵심 이벤트가 됩니다. 중요한 것은 각 서비스에서 사용자가 가장 자주 쓰고, 장애가 나면 가장 크게 영향을 받는 흐름을 먼저 관찰 대상으로 정하는 것입니다.
