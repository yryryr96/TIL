### spring container

```java
ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
```

위와 같이 스프링 컨테이너를 생성할 수 있다.



**|작동 순서**

- 파라미터로 넘겨준 클래스를 조사함.
- AppConfig 클래스로 가서 `@Bean`이 붙은 클래스들을 모두 스프링 컨테이너에 (빈 이름, 빈 객체) 형태로 등록.
- 기본적으로 메서드 명이 빈 이름, 빈 객체에는 참조 값이 할당됨.
  - `@Bean(name = ~~~~)` 로 빈 이름 직접 설정 가능.



### @Configuration

```java
@Configuration
public class AppConfig {

    @Bean
    public MemberService memberService() {

        System.out.println("call AppConfig.memberService");
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public MemoryMemberRepository memberRepository() {
        System.out.println("call AppConfig.memberRepository");
        return new MemoryMemberRepository();
    }

    @Bean
    public OrderService orderService() {
        System.out.println("call AppConfig.orderService");
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    @Bean
    public DiscountPolicy discountPolicy() {
        return new RateDiscountPolicy();
    }
}
```

위와 같이 작성하고 스프링 컨테이너에 등록하면 `call AppConfig.memberRepository`가 3번 출력될 것 같다. 하지만 모두 1번씩만 출력된다.



`@Configuration`이 붙은 `AppConfig`는 `hello.core.AppConfig$$SpringCGLIB$$0` 와 같이 바이트코드가 조작되어 등록된다. 

이 과정에서 `AppConfig`는 내부적으로 `@Bean`이 등록된 메서드마다 이미 빈에 존재하면 빈을 반환하고, 스프링 빈이 없으면 생성해서 스프링 빈으로 등록하고 반환하는 코드가 동적으로 만들어진다.

```java
// 간단한 예시
@Bean
public MemberRepository memberRepository() {
    
    if (MemoryMemberRepository가 이미 스프링 컨테이너에 등록되어 있으면) {
        return 스프링 빈;
    } else {
        스프링 컨테이너에 MemoryMemberRepository를 등록하고
        return 반환;
    }
}
```

