### @ComponentScan

기존 `AppConfig`와 같이 @Configuration 으로 설정 클래스를 생성하고, `@Bean`으로 빈을 생성하고 의존 관계 설정해주는 것은 너무 귀찮고, 개발자가 누락할 수 있다.

`@ComponentScan`은 `@Component`가 붙은 모든 클래스를 빈으로 등록한다.

- @Component
- @Controller
- @Service
- @Repository
- @Configuration

모두 @Comonent가 내부에 적용되어 있다.



```java
@ComponentScan(includeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyIncludeComponent.class),
    elcludeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyexcludeComponent.class))
```

위와 같이 component scan 대상이 되도록 설정하거나, 대상에서 제외되도록 설정할 수 있다.



### @Autowired

필드, 생성자 등 `@Autowired`를 사용하면, 스프링 컨테이너에서 해당 빈을 찾아 주입한다.

기본적으로 ac.getBean()과 같이 동작한다고 생각하면 된다. -> 기본 조회 전략은 타입으로 빈 조회



1. **생성자 주입 (추천)**
   - 생성자 주입은 인스턴스가 생성될 때 한 번만 실행되고 `final`을 사용해 값이 변경되지 않음을 보장할 수 있음.
2. **수정자 주입 (setter)**
   - 수정자 주입을 받을 경우 set 메서드를 public으로 열어놔야하는데 이는 좋지 않은 설계가 될 수 있음.
3. **필드 주입 (비추)**
   - 주입 받은 것을 바꿀 방법이 없음.
4. **메서드 주입 (안쓰임)**



생성자 주입을 제외한 모든 경우는 생성자 이후에 호출되므로 필드에 `final` 키워드를 사용할 수 없음.

**생성자가 딱 1개만 있을 때는 @Autowired 생략 가능**



1. 타입으로 먼저 조회
2. 타입 매칭 결과가 2개 이상일 때 필드 명, 파라미터 명으로 빈 이름 매칭



### @Qualifier

```java
@Component
@Qualifier("mainDiscountPolicy")
public class RateDiscountPolicy implements DiscountPolicy {}

@Component
@Qualifier("fixDiscountPolicy")
public class FixDiscountPolicy implements DiscountPolicy {}

@Autowired
public OrderServiceImpl(MemberRepository memberRepository, @Qualifier("mainDiscountPolicy") DiscountPolicy discountPolicy) {
    this.memberRepository = memberRepository;
	this.discountPolicy = discountPolicy;
}
```

주입시 추가적인 구분자로 사용.

만약 `@Qualifier("mainDiscountPolicy")`에 해당하는 대상이 없다면? mainDiscountPolicy라는 이름의 스프링 빈을 추가로 찾음.

하지만, @Qualifier는 @Qualifier를 찾는 용도로만 사용하는게 명확하고 안전함.



- **@Qualifier끼리 매칭**
- **빈 이름 매칭**
- **NoSuchBeanDefinitionException 예외 발생**



### @Primary

@Autowired 시 타입으로 조회한 스프링 빈이 2개 이상일 때, @Primary가 붙어있는 것이 우선권을 가짐.



ex) 자주 사용하는 DB 커넥션 획득하는 스프링 빈, 가끔 사용하는 서브 데이터베이스의 커넥션을 획득하는 스프링 빈이 있다.

**자주 사용하는 스프링 빈은 @Primary로 우선권을 가지게하여 @Qualifier 지정 없이 편리하게 사용하고, 가끔 사용하는 스프링 빈은 @Qualifier를 사용해 명시적으로 획득하는 방식으로 사용한다면 코드를 깔끔하게 유지 가능.**



### Lombok



#### @RequiredArgsConstructor

final이 붙은 필드를 모아서 생성자를 자동으로 만들어줌.