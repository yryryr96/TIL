# exception



정상 흐름과 예외 처리를 구분하기 위해 사용



1. **잡거나 (try/catch)**
2. **던지거나 (throw, throws)**



### 예외 계층

**Object**

- **Throwable**
  - Error
  - **Exception**
    - **CheckedException**
      - SQLException
      - IOException
      - 등등
    - **UncheckedException**
      - RuntimeException



### **CheckedException**

**Exception**을 상속받는 RuntimeException을 제외한 모든 exception

컴파일러가 확인하는 예외 -> 처리하지 않을 경우 컴파일 오류 발생

개발자가 명시적으로 처리해줘야 함. (잡거나, 던지거나)



- **장점** : 예외 처리를 누락하지 않을 수 있음. 컴파일 시점에 어떤 체크 예외가 발생하는지 쉽게 파악할 수 있다.
- **단점** : 개발자가 모든 체크 예외를 반드시 처리해야 하기 때문에 번거로움. 



### UncheckedException

RuntimeException과 그 하위 예외는 언체크 예외로 분류됨.

언체크 예외는 말 그대로 컴파일러가 예외를 체크하지 않는다는 뜻.

언체크 예외는 체크 예외와 기본적으로 동일. 하지만, **throws를 선언하지 않아도 자동으로 예외를 던짐.**



- **장점** : 신경쓰고 싶지 않은 언체크 예외를 무시할 수 있다.
- **단점** : 실수로 예외 처리를 누락할 수 있음.



### try-with-resources

AutoCloseable 구현체를 사용해서 try 블럭이 끝나는 순간 자동으로 close()를 호출함. -> 빠른 리소스 해제 가능

```java
// NetworkClient
public class NetworkClient implements AutoCloseable {
    
    // etc ...
    
    public void disconnect() {
        system.out.println("서버 연결 해제");
    }
    
    @Override
    public void close() {
        System.out.println("NetworkClient.close");
        disconnect();
    }
}

// NetworkService
public class NetworkService {
    public void sendMessage(String data) {
        
        String address = "http://example.com";
        
        try (NetworkClient client = new NetworkClient(address)) {
            client.initError(address);
            client.connect();
            client.send(data);
        } catch (Exception e) {
            System.out.println("[예외 확인]: " + e.getMessage());
        }
    }
}

// Main
public class Main {
    public static void main(String[] args) {
        NetworkService networkService = new NetworkService();
        Scanner scanner = new Scanner(System.in);
        while (true) {

            System.out.print("전송할 문자: ");
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                break;
            }

            try {
                networkService.sendMessage(input);
            } catch (Exception e) {
                exceptionHandler(e);
            }

            System.out.println();
        }

        System.out.println("프로그램을 정상 종료합니다.");
	}
    
    private static void exceptionHandler(Exception e) {

        System.out.println("사용자 메시지: 죄송합니다. 알 수 없는 문제가 발생했습니다.");
        System.out.println("==개발자용 디버깅 메시지==");
        e.printStackTrace(System.out);

        // 필요하면 예외 별로 처리 가능
        if (e instanceof SendExceptionV4 sendEx) {
            System.out.println("[전송 오류] 전송 데이터: " + sendEx.getSendData());
        }
    }
}
```

try/catch 구문에서 리소스를 해제해야하는 경우 사용하면 좋음.

기존 try/catch  구문의 순서는 **try -> catch -> finally**

try-with-resources 순서는 **try -> close -> catch**

즉, try-with-resources 구문에서 try 블럭을 빠져나오는 순간 AutoCloseable 구현체의 close가 자동으로 호출됨.

try를 빠져나오는 순간 close가 호출되기 때문에 try/catch 구문 보다 빠른 리소스 해제가 가능.

또한, client 의 스코프가 좁아지고, finally를 사용하지 않으므로 코드가 더욱 간결해 보임.