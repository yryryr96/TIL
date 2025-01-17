### MicroService Communication

Spring Framework에서 마이크로서비스 간 통신하는 방법에는 `RestTemplate`, `WebClient`, `FeignClient`가 활용된다.



#### RestTemplate

Spring Framework에서 제공하는 동기식 클라이언트 라이브러리

Blocking I/O 방식을 사용하기 때문에 동시성이 높은 경우에 성능 문제가 발생할 수 있다.

또한, Spring 5.0 이후에는 WebClient가 권장되는 추세이다.



1. ##### 빈 등록

```java
@Bean
@LoadBalanced
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```



2. ##### 사용

```java
UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

List<ResponseOrder> orders = new ArrayList<>();
/* Using as rest template */
String orderUrl = String.format(env.getProperty("order_service.url"), userId);
ResponseEntity<List<ResponseOrder>> orderListResponse =
        restTemplate.exchange(orderUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ResponseOrder>>() {
});

List<ResponseOrder> orderList = orderListResponse.getBody();
```

RestTemplate은 동기식으로 동작하기 때문에, 응답을 받을 때까지 대기하며 스레드를 차단한다. 따라서, 대량의 요청을 처리할 때는 성능 이슈가 발생할 수 있다.



#### WebClient

Spring 5.0에서 출시된 WebFlux의 라이브러리의 일부이다. Non-blocking I/O를 활용한 리액티브 프로그래밍 모델을 기반으로 한다.

이를 통해 확장성과 동시 요청 처리를 활성화시킨다. 동기식으로 동작하게 할 수도 있다.



##### 예제

```java
WebClient webClient = WebClient.create();
String url = "https://api.example.com/users/{id}";
Mono<User> userMono = webClient.get()
        .uri(url, 1)
        .retrieve()
        .bodyToMono(User.class);
출처: https://hahahoho5915.tistory.com/79 [넌 잘하고 있어:티스토리]
```



#### FeignClient

Netflix에서 RestTemplate보다 효율적인 통신을 위해 **선언적 접근 방식**을 활용한 라이브러리

HTTP 클라이언트 인터페이스를 정의하면 클라이언트를 쉽게 구축하고 유지 관리할 수 있다. Spring Cloud와 통합되어 circuit breaker, retry 같은 기능을 제공한다.

Blocking I/O를 사용해 동시성 높은 상황에 문제가 발생할 수 있다.

`spring-cloud-starter-openfeign` 의존성 필요

##### OrderServiceClient

user-service에서 order-service로 통신하기 위해 FeignClient를 만들어줘야 한다.

```java
@FeignClient(name = "order-service") // service discovery 서비스에 등록된 서비스 name 으로 동작 가능
// @FeignClient(name = "order-service", url = "https://{order-service.ip}")
public interface OrderServiceClient {
    @GetMapping("/order-service/{userId}/orders")
    List<ResponseOrder> getOrders(@PathVariable String userId);
}
```

##### UserService

```java
UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
userDto.setOrders(orderList);
```

##### main class

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```



- `선언적인 API 정의` : OpenFeign은 인터페이스 기반으로 RESTful API를 정의한다. 애플리케이션에서 사용하는 외부 서비스의 API를 인터페이스로 작성하고, 각 메서드에는 요청 URL, HTTP 메서드, 요청/응답 형식 등을 어노테이션으로 지정

- `자동화된 요청 처리` : OpenFeign은 정의된 인터페이스를 기반으로 실제 HTTP 요청을 자동으로 생성한다. 이를 통해 개발자는 직접 HTTP 요청을 작성하거나 URL, 헤더, 쿼리 파라미터 등을 관리할 필요가 없다.
- `부가적인 기능 지원` : OpenFeign은 요청과 응답에 대한 부가적인 기능을 제공한다. 예를 들어, 요청과 응답 로깅, 헤더 설정, 에러 핸들링 등의 기능을 제공한다. 



##### ErrorHandling

1. ##### Bean 등록

```java
@Bean
public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
}
```

2. ##### ErrorDecoder 구현 및 빈 등록

```java
@Component
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

    private final Environment env;

    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
            case 400:
                break;
            case 404:
                if (methodKey.contains("getOrders")) {
                    return new ResponseStatusException(HttpStatus.valueOf(response.status()),
                            env.getProperty("order_service.exception.orders_is_empty"));
                }
            default:
                return new Exception(response.reason());
        }

        return null;
    }
}
```

위와 같이 ErrorDecoder를 구현하여 각 상황에 맞는 에러를 관리할 수 있다. 

위와 같은 경우는 `FeignClient`를 사용해 외부 서비스와 통신한 경우에 404 에러가 발생했고, `getOrders`가 포함된 메서드가 실행됐다면 설정파일에 작성된 `order_service.exception.orders_is_empty` 메시지를 반환하도록 코드를 작성했다.



**OpenFeign은 Spring Cloud 프로젝트와 함께 사용될 때 편리하게 통합된다. Eureka와 같은 서비스 디스커버리 클라이언트와 함께 사용하여 마이크로서비스 아키텍처에서 서비스 간의 통신을 쉽게 구성할 수 있다.**