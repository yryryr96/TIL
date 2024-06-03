### 템플릿 메서드 패턴

```java
public abstract class AbstractTemplate {
    
    void execute() {
        // 변하지 않는 부분
        call();
        // 변하지 않는 부분
    }
    
    protected abstract void call(); // 상속 받는 자식 클래스에서 구현
}
```

```java
@Slf4j
public SubClassLogic1 extends AbstractTemplate {
    
    @Override
    protected void call() {
        log.info("로직1 실행");
    }
}
```

- 템플릿 메서드 패턴은 변하지 않는 로직을 `template`에 작성하고, 자식 클래스에서 `call()` 메서드에 필요한 로직을 작성한다.
- 비즈니스 로직 부분과 변하지 않는 부분을 분리할 수 있다.
- **문제점**
  - 상속받는 자식 클래스는 부모의 기능을 사용하지 않는다. -> 부모의 기능을 사용하지 않는데 부모에 의존해야 한다.
  - 즉, 불필요한 상속이 발생한다.



### 전략 패턴

`Context`에 변하지 않는 부분을 작성하고, `Strategy`라는 인터페이스를 구현해서 템플릿 메서드 패턴의 문제점을 해결한다.

```java
public interface Strategy {
    void call();
}
```

```java
@Slf4j
public class StrategyLogic1 implements Strategy {
    @Override
    public void call() {
        log.info("비즈니스 로직1 실행");
    }
}
```

```java
public class Context {
    
    private Strategy strategy;
    
	public Context(Strategy strategy) {
        this.strategy = strategy;
    }
    
    public void execute() {
        // 변하지 않는 부분
        strategy.call();
        // 변하지 않는 부분
    }
}
```

Context에서 Strategy를 주입받아서 사용한다. 



### 템플릿 콜백 패턴

```java
@Slf4j
public class Context {
    
   public void execute(Strategy strategy) {
       // 변하지 않는 부분
       strategy.call();
       // 변하지 않는 부분
   }
}
```

Strategy 인터페이스를 필드로 두지 않고, 메서드를 실행할 때 `Strategy` 인터페이스를 넘겨 받아서 실행한다. 이렇게 하면 전략 패턴보다 조금 더 유연하게 사용할 수 있다.