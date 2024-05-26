### 빈 스코프

- 싱글톤
- 프로토타입
- 웹 관련 스코프



#### 싱글톤 (singleton)

스프링이 제공하는 기본 스코프, 스프링 컨테이너의 시작과 종료까지 유지되는 가장 넓은 범위의 스코프



#### 프로토타입 (prototype)

스프링 컨테이너에서 빈을 조회할 때 새로운 인스턴스를 생성해서 반환 -> 스프링 컨테이너에서 빈을 조회할 때 마다 모두 다른 인스턴스를 반환하고, 그 이후에 스프링 컨테이너에서 관리하지 않음.

스프링 컨테이너에서 빈을 조회할 때 초기화 메서드가 실행됨. 하지만, 소멸 메서드는 클라이언트에서 관리해야 함. 즉, `@PreDestroy` 메서드가 동작하지 않음.



### 주의 !

싱글톤 빈 안에서 프로토타입 빈을 사용할 때 주의해야 할 점이 있다.

싱글톤 빈 클래스 내에 프로토타입 빈 필드가 존재한다면 싱글톤 빈을 조회할 때 마다 그 필드는 새로 생성될 것인가? 그렇지 않다. 싱글톤 빈이 처음 초기화 될 때 프로토타입 빈 필드가 새로 생성되어 주입되고 그 후에 싱글톤 빈은 스프링 컨테이너에서 관리하므로 항상 같은 참조 값을 갖게 된다.

즉, 싱글톤 빈 내에 있는 프로토타입 빈을 자동 주입 받아서 조회하면 항상 같은 인스턴스가 조회된다.

그럼 항상 다른 빈이 조회되도록 하고 싶으면?? `ObjectFactory`, `ObjectProvider`, `Provider` 를 사용하면 된다.

```java
@Scope("singleton")
@RequiredArgsConstructor
static class ClientBean {

    @Autowired
    private ObjectProvider<PrototypeBean> prototypeBeanProvider;

    public int logic() {
        PrototypeBean prototypeBean = prototypeBeanProvider.getObject();
        prototypeBean.addCount();;
        return prototypeBean.getCount();
    }
}
```

ObjectProvider를 사용해서 getObject 메서드를 사용하면 DL (Dependency Loockup) 역할을 한다. 즉, ObjectProvider는 클라이언트와 스프링 컨테이너의 중개자와 같은 역할이다. 

위 코드에서 ObjectProvider를 통해 스프링 컨테이너에 PrototypeBean을 조회하면 새로운 인스턴스를 생성해서 반환한다. 그것을 이용해서 로직을 작성하면 된다.



### 웹 관련 스코프

- request : HTTP 요청 하나가 들어오고 나갈 때 까지 유지되는 스코프
- websocket : 웹 소켓과 동일한 생명 주기를 가지는 스코프
- session : HTTP Session과 동일한 생명 주기를 가지는 스코프
- application : 서블릿 컨텍스트와 동일한 생명 주기를 가지는 스코프



#### request scope

HTTP 요청이 들어오고 나갈 때 까지 유지되는 스코프이다.

`@Scope(value = "request")`로 설정할 수 있다. HTTP 요청이 들어오고 나가는 동안 request scope의 빈을 조회하면 항상 같은 빈이 조회된다.

HTTP 요청 마다 빈이 생성되므로, clientA, clientB가 같은 타입의 빈을 요청하더라도 조회된 빈은 서로 다르다.



#### scope, proxy

request scope는 http request가 있어야 스프링 빈이 생성된다. 이 때, controller나 service 단에서 자동 주입을 사용한다면 request가 없는 시점에 자동 주입이 실행되기 때문에 오류가 발생한다. 이 때, ObjectProvider를 사용하거나 프록시를 사용해서 문제를 해결할 수 있다.

`@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)` 이처럼 설정하면 가짜 프록시 클래스를 만들어두고 HTTP request와 상관 없이 가짜 프록시 클래스를 다른 빈에 미리 주입해 둘 수 있다.



#### 프록시 객체

###### 가짜 프록시 객체는 요청이 오면 그 때 내부에서 진짜 빈을 요청하는 위임 로직이 들어있다.

- 원본 클래스를 상속 받은 객체이다.
- 프록시 객체는 실제 클래스를 찾는 방법을 알고 있다. (참조 값?)
- 클라이언트가 프록시 객체의 메서드를 실행한다면, 프록시 객체는 실제 객체의 메서드를 실행한다. (위임)
- 즉, 클라이언트 입장에서는 원본인지 아닌지 모르게 사용할 수 있다.(다형성)



###### 동작 정리

- CGLIB 라이브러리로 내 클래스를 상속 받은 가짜 프록시 객체를 만들어서 주입한다.
- 가짜 프록시 객체는 실제 요청이 오면 그떄 내부에서 실제 빈을 요청하는 위임 로직이 있다.
- 프록시 객체는 request scope와는 상관 없다. 그냥 가짜이고, 단순한 위임 로직만 있고, **싱글톤 처럼 동작한다.**



###### 특징

- 프록시 객체 덕에 클라이언트는 마치 싱글톤 빈을 사용하듯 편리하게 request scope를 사용할 수 있다.
- Provider를 사용하든, 프록시를 사용하든 핵심 아이디어는 진짜 객체 조회를 꼭 필요한 시점까지 지연 처리 한다는 점.



###### 주의점

- 마치 싱글톤을 사용하는 것 같지만 다르게 동작하기 때문에 결국 주의해서 사용해야 한다.
  - **프록시 객체는 싱글톤으로 동작하지만, 위임되는 원본 클래스는 다른 스코프일 수 있다.**
- 특별한 scope는 꼭 필요한 곳에만 최소화해서 사용하자. 무분별하게 사용하면 유지보수가 어렵다.