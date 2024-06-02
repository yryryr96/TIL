### @Transactional

- 스프링의 선언적 트랜잭션 적용 방법
- 스프링은 선언적 트랜잭션을 적용하면 Proxy를 생성해서 트랜잭션 관련 작업을 처리해준다.

```java
// 선언적 트랜잭션 적용
@Transactional
public class TestService {
    ...
}

// 빈 등록
@Bean
public class TestService testService() {
    return new TestService();
}

//프록시 확인
log.info("class = {}", testService.getClass()); // TestService$$SpringCGLIB$$0
```



#### 프록시 내부 호출

```java
@Slf4j
static class CallService {
    
    public void external() {
        log.info("call external");
        printTxInfo();
        internal();
    }
    
    @Transactional
    public void internal() {
        log.info("call internal");
        printTxInfo();
    }
    
    private void printTxInfo() {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("tx active={}", txActive);
    }
}
```

```java
@Test
void externalCall() {
    callService.external();
}
```

CallService의 external() 메서드를 실행하면 internal() 메서드에서 트랜잭션이 적용될 것 같지만 그렇지 않다. 



![image-20240602185158507](..\images\image-20240602185158507.png)

1. callService 프록시 호출
2. `@Transactional` 확인
3. 트랜잭션 적용 없이 실제 callService의 external() 메서드 실행
4. external() 메서드에서 this.internal() 실행 -> 이 때 트랜잭션 적용 안된 상태



callService **프록시**에서 `@Transactional` 애노테이션을 확인하고 트랜잭션 관련 처리를 하는 것이지, 실제 callService 객체에서는 트랜잭션 관련 처리를 하지 않음. -> **단순 메서드 호출**



internal() 메서드를 따로 클래스로 분리해서 사용하면 문제를 해결할 수 있다.

```java
@Slf4j
@RequiredArgsConstructor
static class CallService {
    
    private final InternalService innerService;
    
    public void external() {
        log.info("call external");
        printTxInfo();
        innerService.internal();
    }
    
    private void printTxInfo() {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("tx active={}", txActive);
    }
}

@Slf4j
static class InternalService {
	
    @Transactional
    public void internal() {
        log.info("call internal");
        printTxInfo();
    }
    
    private void printTxInfo() {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("tx active={}", txActive);
    }
}
```

- 여기서 callService에 주입되는 InternalService는 프록시 객체이다. (메서드에 선언적 트랜잭션이 적용되어 있다.)
- 따라서, callService의 external 메서드는 트랜잭션이 적용되지 않고, `innerService.internal()`은 프록시 객체를 호출하는 것이므로 트랜잭션이 적용된다.



#### readOnly 옵션

`@Transactional(readOnly = true)`

- **프레임워크**
  - JdbcTemplate은 읽기 전용 트랜잭션 안에서 변경 기능을 실행하면 예외를 던진다.
  - JPA는 읽기 전용 트랜잭션의 경우 커밋 시점에 플**러시를 호출하지 않는다**. 추가로, 변경이 필요 없으니 **변경 감지를 위한 스냅샷 객체도 생성하지 않는다.**

- **JDBC 드라이버**
  - 읽기 전용 트랜잭션에서 변경 쿼리가 발생하면 예외를 던진다.
  - 읽기, 쓰기(마스터, 슬레이브) 데이터베이스를 구분해서 요청한다.
- **데이터베이스**
  - 데이터베이스에 따라 읽기 전용 트랜잭션의 경우 읽기만 하면 되므로, 내부에서 성능 최적화가 발생한다.



#### transaction exception 처리

스프링은 기본적으로 **체크 예외**는 **비즈니스 의미**가 있을 때, **언체크 예외**는 **복구 불가능한 예외**로 가정한다.

`@Transactional` 애노테이션은 **기본적으로** 트랜잭션 과정에서 `체크 예외`가 던져지면 `commit`, `언체크 예외`가 던져지면 `rollback` 한다.

`rollbackFor` 과 같은 옵션을 통해 Exception에 따른 동작을 설정할 수 있으므로 고정적인 것이 아니라 기본 설정임을 주의하자!



###### 예시

1. **정상** : 주문시 결제를 성공하면 주문 데이터를 저장하고 결제 생태를 **완료**로 처리한다.

2. **시스템 예외** : 주문시 내부에 복구 불가능한 예외가 발생하면 전체 데이터를 **롤백**한다.

3. **비즈니스 예외** : 주문시 결제 잔고가 부족하면 주문 데이터를 **저장**하고, 결제 상태를 **대기**로 처리한다.

   -> 고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내

