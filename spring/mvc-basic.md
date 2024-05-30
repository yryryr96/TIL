### mvc 흐름

1. 클라이언트 HTTP 요청
2. Dispatcher Servlet에서 핸들러 매핑
3. Dispatcher Servlet에서 핸들러 어댑터 조회
4. 핸들러 어댑터로 핸들러 실행
5. 핸들러 어댑터에서 ModelAndView 반환
6. viewResolver 호출
7. View 반환
8. render(model) 호출
9. HTML 응답



######  HandlerMapping

0 순위 = RequestMappingHandlerMapping : 애노테이션 기반 컨트롤러 @RequestMapping

1 순위 = BeanNameUrlHandlerMapping : 스프링 빈 이름으로 핸들러를 조회



###### HandlerAdapter

0 순위 = RequestMappingHandlerAdapter : 애노테이션 기반 컨트롤러 @RequestMapping

1 순위 = HttpRequestHandlerAdapter : HttpRequestHandler 처리

2 순위 = SimpleControllerHandlerAdapter : Controller 인터페이스 (애노테이션 x, 과거에 사용) 처리



- 요청이 들어오면 Dispatcher Servlet에서 HandlerMapping 으로 handler 조회 
- HandlerAdapter에서 supports 메서드로 handler를 실행할 수 있는 어댑터 조회
- 어댑터로 핸들러 실행
- 핸들러 실행해서 반환받은 값으로 어댑터에서 ModelAndView 생성해서 반환



### 요청 파라미터 조회

GET 요청이나 http form tag 요청은 request.getParameter() 로 파라미터 정보를 조회할 수 있다.

- **@ModelAttribute**
- **@RequestParam**

###### @ModelAttribute

```java
@ResponseBody
@RequestMapping("/model-attribute-v1")
public String modelAttributeV1(@ModelAttribute HelloData helloData) {
 	log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
	return "ok";
}
```

1. HelloData 객체 생성
2. 요청 파라미터 이름으로 HelloData 프로퍼티 조회 -> `프로퍼티` : getter, setter 메서드를 가지고 있는 필드 
3. 해당 프로퍼티의 setter를 호출해 파라미터 값을 바인딩



**@RequestParam, @ModelAttribute는 생략이 가능하다.**

생략했을 때 바인딩 타입이 단순 타입(String, int, Integer 등)이라면 @RequestParam, 그 외에는 @ModelAttribute로 적용된다.



### http 메시지 바디 조회

post, patch, delete와 같은 요청에서 메시지 바디에 담긴 데이터를 조회. (주로 JSON)

- **HttpEntity<>().getBody()**
- **@RequestBody**



### HttpMessageConverter

HTTP 요청의 메시지 바디에 들어있는 데이터를 처리할 때 사용된다.

- `ByteArrayHttpMessageConverter` : byte[] 데이터 처리
- `StringHttpMessageConverter` : String 문자로 데이터 처리
- `MappingJackson2HttpMessageConverter` : 객체 또는 HashMap



###### HttpMessageConverter는 어디에서 위치하며 작동할까? -> RequestMappingHandlerAdapter에서 ArgumentResolver, ReturnValueHandler에 포함 된다.

- `ArgumentResolver`
  - 애노테이션 기반 컨트롤러에는 다양한 파라미터 바인딩이 가능하다.
  - 컨트롤러(핸들러)가 필요로 하는 다양한 파라미터의 값(객체)을 생성한다. 파라미터 값이 모두 준비되면 컨트롤러를 호출하며 값을 넘겨준다.
- `ReturnValueHandler`
  - ArgumentResolver와 비슷하게 응답 값을 변환하고 처리한다.



**요청의 경우** @RequestBody, HttpEntity를 처리하는 ArgumentResolver가 각각 있다. (RequestResponseBodyMethodProcessor, HttpEntityMethodProcessor) 이  ArgumentResolver들이 HttpMessageConverter를 사용해서 필요한 객체를 생성한다.



**응답의 경우** @ResponseBody,HttpEntity를 처리하는 ReturnValueHandler가 각각 있다. 그리고 여기에서 HttpMessageConverter를 사용해서 응답 결과를 생성한다.



### 정리

1. http 요청
2. Dispatcher Servlet에서 handler 조회
3. Dispatcher Servlet에서 handler adapter 조회
4. handler adapter에서 handler에 넘길 파라미터를 ArgumentResolver로 처리 (이 과정에서 HttpMessageConverter가 사용됨)
5. handler 호출, 처리
6. ReturnValueHandler에서 handler의 반환 값을 변환
7. Dispatcher Servlet에서 handler adapter 실행 결과로 반환 받은 값으로 HTTP 응답