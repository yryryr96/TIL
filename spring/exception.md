### 서블릿 예외 처리

- Exception
- response.sendError(HTTP 상태 코드, 오류 메시지)



###### 예외 발생 흐름

```
컨트롤러(예외 발생) -> 인터셉터 -> 서블릿 -> 필터 -> WAS(여기까지 전파)
```

###### sendError 흐름

```
컨트롤러(response.sendError()) -> 인터셉터 -> 서블릿 -> 필터 -> WAS(sendError 호출 기록 확인)
```

###### 오류 페이지 요청 흐름

```
WAS (해당 예외를 처리하는 오류 페이지 정보 확인) `/error-page/500` 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> View
```



##### DispatcherType

오류가 발생하면 WAS에서 오류 페이지를 호출한다. 이 과정에서 필터와, 인터셉터도 다시 호출하게 되는데 이는 비효율적이다. 따라서, 클라이언트로 발생한 정상 요청인지, 서버 내부에서 오류 페이지를 호출하기 위한 요청인지 구분이 필요. 이 때 사용되는 것이 `DispatcherType`

```java
// jakarta.servlet.DispatcherType
public enum DispatcherType {
    FORWARD,
    INCLUDE,
    REQUEST,
    ASYNC,
    ERROR
}
```

- REQUEST : 클라이언트 요청
- ERROR : 오류 요청
- FORWARD : MVC에서 배웠던 서블릿에서 다른 서블릿이나 JSP 호출할 때
- INCLUDE : 서블릿에서 다른 서블릿이나 JSP의 결과를 포함할 때
- ASYNC : 서블릿 비동기 호출



##### 전체 흐름 정리

- 정상 요청

```
WAS (DispatcherType.REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러 -> View
```

- 오류 요청

```
1. WAS (DispatcherType.REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러 (예외 발생)
2. WAS (DispatcherType.ERROR, 오류 페이지 조회) -> 필터 x -> 서블릿 -> 인터셉터 x -> 컨트롤러 (오류 페이지) -> View
```



### ExceptionResolver

컨트롤러에서 예외가 발생하면 ExceptionResolver가 우선순위에 따라 실행되고 예외를 처리한다. 예외가 서블릿에 전달되기 전에 처리하므로 정상 흐름을 유지할 수 있다.

- `ExceptionHandlerExceptionResolver`

  : @ExceptionHandler를 처리한다.

- `ResponseStatusExceptionResolver`

  : HTTP 상태 코드를 지정해준다. ex. @ResponseStatus(value = HttpStatus.NOT_FOUND)

- `DefaultHandlerExceptionResolver`

  : 스프링 내부 기본 예외를 처리한다. ex. typeMismatchException



###### ExceptionHandlerExceptionResolver 실행 흐름

1. 컨트롤러에서 `IllegalArgumentException` 예외가 컨트롤러 밖으로 던져진다.
2. 예외가 발생했으므로 `ExceptionResolver`가 작동한다. 가장 우선순위가 높은 `ExceptionHandlerExceptionResolver`가 실행된다.
3. `ExceptionHandlerExceptionResolver`는 해당 컨트롤러에 `IllegalArgumentException`을 처리할 수 있는 `@ExceptionHandler`가 있는지 확인한다.
4. 있으면 실행



### ControllerAdvice

- ExceptionHandlerExceptionResolver가 컨트롤러 내에 있는 @ExceptionHandler를 찾아서 예외를 처리한다. 하지만, 이렇게 되면 정상 코드와 예외 처리 코드가 하나의 컨트롤러에 섞이는 문제가 발생한다.

- `ControllerAdvice`, `RestControllerAdvice` 를 사용해서 이 문제를 해결할 수 있다.
- RestControllerAdvice는 ControllerAdvice에 @ResponseBody가 붙은 것이다.

```java
@RestControllerAdvice
public class ExControllerAdvice {
    
    @ExceptionHandler
    public ResponseEntity<ErrorResult> userExHandle(UserException e) {

        ErrorResult errorResult = new ErrorResult("user-ex", e.getMessage());
        return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
    }
}
```

위 코드와 같이 사용할 수 있다. -> 예외 처리를 별도의 클래스를 생성해서 한 곳에서 진행



```java
// 특정 애노테이션이 적용된 컨트롤러 지정
@ControllerAdvice(annotations = RestController.class)
public class ExampleAdvice1 {}

// 패키지 내에 존재하는 컨트롤러 지정
@ControllerAdvice("org.example.controllers")
public class ExampleAdvice2 {}

// 클래스 지정
@ControllerAdvice(assignableTypes = {ControllerInterface.class, AbstractController.class})
public class ExampleAdvice3 {}
```

- 위와 같이 ControllerAdvice가 적용될 컨트롤러를 지정할 수 있다.
- 지정하지 않으면 글로벌 적용



### 정리

- 컨트롤러에서 exception 발생해서 WAS까지 전달되면 예외에 매핑된 컨트롤러 조회해서 바인딩 (기본 값 존재)
- HandlerExceptionResolver를 직접 구현해서 서블릿에서 정상 흐름이 되도록 처리할 수 있음.
- 스프링은 기본으로 제공하는 ExceptionResolver가 있음.
  - ExceptionHandlerExceptionResolver
  - ResponseStatusExceptionResolver
  - DefaultHandlerExceptionResolver

```
HTTP 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러 (오류 발생) -> ExceptionResolver 실행 -> 예외 처리 -> 서블릿 정상 흐름 반환
```

