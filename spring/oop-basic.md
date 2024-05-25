### SOLID

- SRP (Single Responsibility Principle, 단일 책임 원칙)
- OCP (Open-Close Principle, 개방 폐쇄 원칙)
- LSP (Liscov SUbstitution Principle, 리스코프 치환 원칙)
- ISP (Interface Segragation Principle, 인터페이스 분리 원칙)
- DIP (Dependency Inversion Principle, 의존관계 역전 원칙)



```java
public class OrderServiceImpl implements OrderService {

//    private DiscountPolicy discountPolicy = new FixDiscountPolicy();
    private final MemberRepository memberRepository = new MemoryMemberRepository();
    private final DiscountPolicy discountPolicy = new RateDiscountPolicy();

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {

        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);

        return new Order(memberId, itemName, itemPrice, discountPrice);
    }
}
```

위와 같은 코드는 잘 설계된 코드인 것 같지만 실로 아니다.

`OrderServiceImpl` 은 인터페이스인 `MemberRepository`, `DiscountPolicy`를 의존하는 동시에 구현체인 `MemoryMemberRepository`, `RateDiscountPolicy`도 의존하고 있다. 이는 DIP를 위반하는 것이며, 다른 구현체를 사용하기 위해서는 코드를 변경해야 하기 때문에 OCP도 위반한다.



이를 해결하기 위해 인터페이스에 구현체 주입을 위한 `AppConfig` 생성

```java
public class AppConfig {

    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    public MemoryMemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    public DiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }
}
```

```java
public static void main(String[] args) {

    AppConfig appConfig = new AppConfig();
    MemberService memberService = appConfig.memberService();
    OrderService orderService = appConfig.orderService();

    Long memberId = 1L;
    Member member = new Member(memberId, "memberA", Grade.VIP);
    memberService.join(member);

    Order order = orderService.createOrder(memberId, "itemA", 10000);
    System.out.println("order = " + order);
}
```

즉, OrderServiceImpl에 있는 인터페이스 구현체를 주입하는 행위는 모두 AppConfig에서 일어나며, `OrderServiceImpl`은 인터페이스의 동작만 수행하면 된다.

이렇게 함으로써, `OrderServiceImpl`은 인터페이스에만 의존하게 된다. -> **DIP 문제 해결**



**| 정리**

구성 부분, 작동 부분으로 나뉘어서 OCP, DIP 문제를 해결할 수 있었다.

다형성 만으로는 SOLID를 지키면서 좋은 객체 지향 코드를 작성하기엔 무리. 즉, 다른 무언가가 더 필요.