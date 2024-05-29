### 필터 - Filter

##### 필터 흐름

```
HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러
```

필터를 적용하면 필터가 호출된 다음에 서블릿이 호출된다.



##### 필터 흐름

```
HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러
HTTP 요청 -> WAS -> 필터 (적절하지 않은 요청이라 판단, 서블릿 호출 x)
```



##### 필터 체인

```
HTTP 요청 -> WAS -> 필터1 -> 필터2 -> 필터3 -> 서블릿 -> 컨트롤러
```

필터는 체인으로 구성되는데, 중간에 필터를 자유롭게 추가할 수 있다. 로그 필터, 로그인 체크 필터 등등



##### 필터 인터페이스

```java
public interface Filter {
    
    public default void init(FilterConfig filterConfig) throws ServletException {}
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException;
    
    public default void destroy() {}
}
```

필터 인터페이스를 구현하고 등록하면 **서블릿 컨테이너가 필터를 싱글톤 객체로 생성하고, 관리**한다.

- `init()` : 필터 초기화 메서드, 서블릿 컨테이너가 생성될 때 호출된다.
- `doFilter()` : 고객의 요청이 올 때 마다 해당 메서드가 호출된다. 필터의 로직을 구현하면 된다.
- `destroy()` : 필터 종료 메서드, 서블릿 컨테이너가 종료될 때 호출된다.



### 스프링 인터셉터 - Interceptor

스프링 인터셉터도 서블릿 필터와 같이 웹과 관련된 공통 관심 사항을 효과적으로 해결할 수 있는 기술이다. 

서블릿 필터가 서블릿이 제공하는 기술이라면, 스프링 인터셉터는 스프링 mvc가 제공하는 기술이다.



###### 스프링 인터셉터 흐름

```
HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러
```

- 스프링 인터셉터의 시작점이 디스패처 서블릿
- 스프링 인터셉터에도 URL 패턴을 적용할 수 있는데, 서블릿 URL 패턴과는 다르고, 매우 정밀하게 설정할 수 있다.



###### 스프링 인터셉터 제한

```
HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러
HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 (적절하지 않은 요청이라 판단, 컨트롤러 호출 x)
```



###### 스프링 인터셉터 체인

```
HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 인터셉터1 -> 인터셉터2 -> 컨트롤러
```

필터와 마찬가지로 스프링 인터셉터도 체인으로 구성되어 있다.



**스프링 인터셉터는 서블릿 필터보다 편리하고, 더 정교하고 다양한 기능을 제공한다.**



###### 스프링 인터셉터 인터페이스

```java
public interface HandlerInterceptor {
    
    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {}
    
    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nuallable ModelAndView modelAndView) throws Exception {}
    
    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nuallable Exception ex) throws Exception {}
}
```

서블릿 필터는 로직 작성 메서드가 `doFilter()` 하나만 존재한다. 인터셉터는 `preHandle` 호출 전, `postHandle` 호출 후, `afterCompletion` 요청 완료 이후와 같이 단계적으로 더 세분화 되어 있다.

또한, 서블릿 필터는 request, response만 제공했지만, 인터셉터는 Dispatcher Servlet 이후에 존재하기에 어떤 컨트롤러가 호출되는지 `handler` 정보도 받을 수 있다. 또한, 어떤 `modelAndView`가 반환되는지 응답 정보도 받을 수 있다.



###### 인터셉터 호출 흐름

**정상 흐름**

```
HTTP 요청 -> preHandle -> HandlerAdapter에서 handler 실행 -> ModelAndView 반환 -> postHandle -> render(model) -> afterCompletion -> HTML 응답
```

- `preHandle` : 컨트롤러 호출 전에 호출된다. (핸들러 어댑터 호출 전에 호출 된다.)
  - 응답 값이 true면 다음으로 진행, false면 다음 인터셉터는 물론, 핸들러 어댑터도 호출되지 않음.
- `postHandle` : 컨트롤러 호출 후에 호출된다. (핸들러 어댑터 호출 후에 호출 된다.)
- `afterCompletion` : 뷰가 렌더링 된 이후에 호출된다.



###### 스프링 인터셉터 예외 상황

- `postHandle` : 컨트롤러에서 예외가 발생하면 호출되지 않음.
- `afterCompletion` : 항상 호출된다. 이 경우 예외를 받아서 어떤 예외가 발생했는지 로그로 출력 가능

afterCompletion은 예외가 발생해도 호출되므로 예외와 무관하게 공통 처리를 하려면 afterCompletion을 사용해야 한다.