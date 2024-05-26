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