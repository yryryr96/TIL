### Web Server

- HTTP 기반으로 동작
- **정적 리소스 제공**, 기타 부가기능
- 정적(파일) HTML, CSS, JS, 이미지, 영상
- **nginx, apache 등**



### WAS - Web Application Server

- HTTP 기반으로 동작
- **웹 서버 기능 포함** (정적 리소스 제공 가능)
- **프로그램 코드를 실행해서 애플리케이션 로직 수행**
  - 동적 HTML, HTTP API(JSON)
  - 서블릿, JSP, 스프링 MVC
- **Tomcat, Jetty, Undertow 등**



### Servlet

클라이언트 요청을 처리하고, 그 결과를 반환하는 Servlet 클래스의 구현 규칙을 지킨 자바 웹 프로그래밍 기술.

간단히 말하면, 서블릿이란 **자바를 사용해 웹을 만들기 위해 필요한 기술**이다.



###### 동작 순서

1. 클라이언트가 웹 서버에 요청을 보내면 웹 서버는 WAS에 위임
2. **서블릿 컨테이너에서 request, response 객체를 생성** (request: 데이터가 담긴 객체, response : 빈 객체)
3. 컨테이너는 web.xml 또는 @WebServlet을 기반으로, 요청 URL과 매핑된 서블릿 객체를 찾는다.
4. **해당 서블릿의 최초 요청이었을 경우, 서블릿 객체를 생성하고 init method를 호출한 뒤, service method를 호출해 비즈니스 로직을 수행한다. (request 객체의 데이터를 사용해서 response 객체에 데이터를 담는다.)**
5. **컨테이너는 response 객체에 담긴 데이터로 HTTP 응답 메시지를 생성하고 웹 브라우저로 전송한다.**
6. 전송 완료 후, 서블릿 컨테이너는 두 request, response 객체를 소멸시킨다.



```java
@WebServlet(name = "testServlet", urlPatterns = "/test")
public class TestServlet extends HttpServlet {
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 동작 
        // request 객체 정보 활용 가능 (ex. 파라미터, 메서드, 호스트 등)
        // response 헤더, 쿠키, 세션 등등 조작 가능
    }
}
```

`/test`로 접속하면 `HttpServletRequest`, `HttpServletResponse` 객체가 생성되고, 해당 서블릿인 `TestServlet`이 실행된다.

서블릿은 기능을 수행한 후 결과를 반환하여 클라이언트에 전송한다.



###### 특징

- 클라이언트 request에 대해 동적으로 작동하는 웹 어플리케이션 컴포넌트
- 기존 정적 웹 프로그램의 문제점을 보완해 동적인 여러가지 기능을 제공
- java thread를 이용해 동작
- mvc패턴에서 컨트롤러로 이용됨.
- 서블릿 컨테이너에서 실행
- 보안 기능을 적용하기 쉬움



### Servlet Container

서블릿을 관리하고 실행시키는 또 다른 프로그램이며, Servlet Engine, Web Container 라고도 불린다. 서**블릿 컨테이너는 개발자가 자바 웹 애플리케이션을 개발할 때 비즈니스 로직 작성에만 전념할 수 있도록, 요청이 올 때마다 웹 서버에서 반복적으로 처리해야하는 작업을 대신 수행한다.**



###### 반복적인 작업

1. 서버 TCP/IP 연결 대기, 소켓 연결
2. Request, Response 객체 생성
3. HTTP 요청 메시지 파싱 후, Request 객체에 데이터 넣기
4. Response 객체에 담긴 데이터로 HTTP 응답 메시지 작성
5. TCP/IP에 응답 전달, 소켓 종료



######  역할

1. **서블릿 Life Cycle 관리** 

   : 서블릿 객체의 생성, 초기화, 호출, 종료를 관리한다. 서블릿 객체는 싱글톤으로 관리된다.

2. **서블릿과 웹 서버의 통신 지원** 

   : 서블릿 컨테이너는 서블릿과 웹 서버가 통신할 수 있도록 해준다. 개발자는 비즈니스 로직에 집중할 수 있음.

3. **동시 요청을 위한 멀티스레드 처리 지원**

   : 서블릿 컨테이너는 요청이 올 때마다 새로운 자바 스레드를 생성한다. 동시에 여러 요청이 오더라도 멀티스레드 환경으로 동시에 작업을 수행할 수 있다.

### 

### Servlet 생명주기(Life Cycle)

1. `init()` : 서블릿 객체를 사용하기 위해 선행되어야 하는 **초기화 작업을 수행**한다. 일반적으로 클라이언트의 최초 요청 시점에 서블릿 객체가 컨테이너에 생성되고 init method가 실행된다. 하지만, 초기화는 상대적으로 시간이 걸리는 작업이기 때문에, 서블릿 컨테이너를 처음 구동하는 시점에 초기화를 진행하는 것이 좋다. 또, 해당 메서드는 **서블릿 객체당 한번씩만 호출**되는데 그 이유는 서블릿 객체는 **싱글톤으로 관리**되기 때문이다.

2. `service()` : 호출된 서블릿 객체의 **비즈니스 로직을 수행**한다. HTTP 요청 타입(GET, POST, PUT, DELETE)에 맞는 적절한 메서드(doGet, doPost, doPut, doDelete)를 호출하고 동적 웹 페이지를 만드는 역할을 수행한다.
3. `destroy()` : 서블릿 컨테이너가 **서블릿 객체를 종료**한다. 서블릿 객체는 하나로 재사용(공유)하기 때문에 **init()과 동일하게 한번만 호출**된다.