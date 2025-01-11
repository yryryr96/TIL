### Spring Cloud Gateway

API Gateway를 구축함으로써 여러 microservice 요청에 대해 필터, 로깅, 모니터링, 라우팅 등 다양한 작업을 할 수 있다.

클라이언트의 진입점을 한 곳(API Gateway)로 통일할 수 있고, 요청에 대한 다양한 작업이 가능하다.

![image-20250112002406963](../images/image-20250112002406963.png)

왜 tomcat 기반이 아니라 netty 기반일까 ?

- ##### 높은 성능

  - `비동기 I/O와 이벤트 기반 처리`: 많은 요청을 효율적으로 처리 (고성능)
  - `다중 연결 처리` : API Gateway는 수많은 클라이언트와 서비스 간의 통신을 중계, Netty는 대규모 연결을 동시에 처리할 수 있다. 즉, 많은 트래픽을 처리하는 API Gateway에 적합

- ##### 낮은 지연 시간 (Low Latency)

  - `빠른 I/O 처리` : Netty는 요청을 처리하는 시간이 짧다. -> 고속 응답 -> **실시간** 요구 높은 시스템에 효과적

- ##### 확장성

  - `커스터마이징 가능한 파이프라인`
    - Netty는 파이프라인 구조를 사용해 요청을 처리
    - API Gateway의 라우팅, 인증, 로깅, 모니터링 등 다양한 기능을 구현 및 최적화 할 수 있다.
  - `프로토콜 지원`
    - HTTP, WebSocket, gRPC, TCP/UDP 등 다양한 프로토콜 지원



##### Filter

- GlobalFilter, CustomFilter로 세부 적용 가능

- `AbstractGatewayFilterFactory` 클래스를 상속받아 `apply` 메서드를 오버라이딩해서 필터를 적용할 수 있다.

```java
package com.example.apigatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        // Custom Pre Filter
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Custom PRE filter: request id -> {}", request.getId());

            // Custom Post Filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("Custom POST filter: response code -> {}", response.getStatusCode());
            }));
        };
    }

    public static class Config {
        // configuration 정보

    }
}
```

위와 같이 `CustomFilter` 클래스를 작성하고 `application.yml`에 명시함으로써 특정 요청에 대해 Filter를 적용할 수 있다.

```yaml
spring:
  application:
    name: apigateway-service
  cloud:
    gateway:
      default-filters:
        - name: GlobalFilter
          args:
            baseMessage: Spring Cloud Gateway Global Filter
            preLogger: true
            postLogger: true
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/user-service/**
            - Method=GET
          filters:
            - RemoveRequestHeader=Cookie
            - RewritePath=/user-service/(?<segment>.*), /$\{segment} # 앞, 뒤 -> 앞 형태 uri를 뒤 형태의 uri로 변경
            - AuthorizationHeaderFilter
```

- `spring.cloud.gateway.default-filters` : api-gateway를 거치는 모든 요청에 적용할 필터 -> `GlobalFilter`
- `spring.cloud.gateway.routes` : 특정 요청에 대해 routing
  - `id` : spring application name
  - `uri` : 라우팅 될 uri
    - `lb://USER-SERVICE` : Eureka Server에 등록된 USER-SERVICE라는 application으로 라우팅
  - `predicates` : 라우팅 조건
    - `Path` : 요청 경로
    - `Method` : 요청 Http Method
  - `filters` : 해당 요청에 적용될 필터
    - 우리가 작성한 `AuthorizationHeaderFilter`를 명시함으로써 /user-service/ 경로를 가진 모든 GET 요청에는 해당 필터가 적용된다.

