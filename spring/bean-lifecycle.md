#### **스프링 빈 라이프 사이클** **(객체 생성 -> 의존관계 주입)**

스프링 빈은 객체를 생성하고, 의존관계 주입이 모두 끝난 다음에야 필요한 데이터를 사용할 수 있는 준비가 완료된다. 따라서, 초기화 작업은 의존관계 주입이 모두 완료되고 난 다음에 호출해야 한다.



#### 스프링 빈 이벤트 라이프 사이클

스프링 컨테이너 생성 -> 스프링 빈 생성 -> 의존관계 주입 -> 초기화 콜백 -> 사용 -> 소멸전 콜백 -> 스프링 종료

- **초기화 콜백** : 빈이 생성되고, 빈의 의존관계 주입이 완료된 후 호출
- **소멸전 콜백** : 빈이 소멸되기 직전에 호출



### 빈 생명주기 콜백

- 인터페이스 (InitializingBean, DisposableBean)
- 설정 정보에 초기화 메서드, 종료 메서드 지정
- @PostConstruct, @PreDestory 애노테이션 지원



#### InitializingBean, DisposableBean

```java
public class NetworkClient implements InitializingBean, DisposableBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        connect();
        call("초기화 연결 메시지");
    }
    
    @Override
    public void destroy() throws Exception {
        disconnect();
    }
}
```

- 초기화 메서드가 주입 완료 후에 적절하게 호출됨.
- 스프링 컨테이너 종료가 호출되면 destroy 소멸 콜백이 호출됨.



**| 단점**

- 스프링 전용 인터페이스, 해당 코드가 스프링 전용 인터페이스에 의존한다.
- 초기화, 소멸 메서드의 이름을 변경할 수 없다. (오버라이딩)
- 내가 코드를 고칠 수 없는 외부 라이브러리에 적용할 수 없다.



#### 빈 등록 초기화, 소멸 메서드 지정

```java
// 설정
public class NetworkClient {

    public void init() {
        connect();
        call("초기화 연결 메시지");
    }

    public void close() {
        disconnect();
    }
}

// 사용
@Bean(initMethod = "init", destroyMethod = "close")
public NetworkClient networkClient() {
    NetworkClient networkClient = new NetworkClient();
    networkClient.setUrl("http://hello-spring.dev");
    return networkClient;
}
```

`@Bean`으로 스프링 빈 등록할 때 `initMethod`, `destroyMethod`로 초기화, 소멸 메서드를 지정할 수 있음.



**| 종료 메서드 추론**

- @Bean의 destoryMethod의 기본 값이 (inferred) (추론) 임.
- 이 추론 기능은 `close`, `shutdown`과 같은 이름의 메서드를 자동으로 호출해준다.
- 따라서, 직접 스프링 빈으로 등록하면 종료 메서드는 따로 적어주지 않아도 잘 작동함.
- 추론 기능을 사용하기 싫으면 `destoryMethod=""` 빈 공백을 지정해주면 된다.



#### 애노테이션 @PostConstruct, @PreDestroy

```java
// 설정
public class NetworkClient {
	
    @PostConstruct
    public void init() {
        connect();
        call("초기화 연결 메시지");
    }

    @PreDestroy
    public void close() {
        disconnect();
    }
}

// 사용
@Bean
public NetworkClient networkClient() {
    NetworkClient networkClient = new NetworkClient();
    networkClient.setUrl("http://hello-spring.dev");
    return networkClient;
}
```

`@PostContruct`, `@PreDestroy`  애노테이션 두개로 초기화, 소멸 메서드를 실행할 수 있다.



- 최신 스프링에서 가장 권장하는 방법
- 패키지가 `javax.annotation.PostConstruct`이다. 즉, 스프링 종속 기술이 아니라 자바 표준임. 따라서, 스프링이 아닌 다른 컨테이너에서도 동작한다.
- 컴포넌트 스캔과 잘 어울림.
- 외부 라이브러리에는 적용하지 못한다는 단점. -> 외부 라이브러리에 적용 위해서는 @Bean의 기능을 사용

