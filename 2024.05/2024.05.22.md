# 2024.05.22 TIL



### ENUM - 열거형



**String 클래스는 타입 안정성이 떨어짐.** 

예를들어, BASIC, GOLD, DIAMOND 등급이 있고 등급별로 할인률이 정해진다고 할 때, 등급을 String으로 받게 된다면 오타가 나거나 등급에 속하지 않은 문자열들이 입력될 수 있음.

이를 방지하기 위해 BASIC, GOLD, DIAMOND 만 가지고 있는 타입으로 입력을 받아야함. 

- 컴파일 시점에 오류를 잡을 수 있음.
- 오타, 등급에 속하지 않은 입력 잡을 수 있음.
- 타입이 명확함. 개발자들간 오해 방지 가능



```java
public enum Grade {
    BASIC, GOLD, DIAMOND
}
```

위와 같이 Enum 타입을 작성하고 `Grade.BASIC`, `Grade.GOLD`, `Grade.DIAMOND` 와 같이 사용하면 타입 안정성과, 오류, 오타 모두 방지할 수 있음.

- Enum 타입은 Class이고 Enum 클래스를 상속받은 것. 
- Enum 타입을 new 로 새로운 인스턴스를 생성할 수 없음. 내부적으로 private 기본생성자로 구현되어있기 때문. -> 타입 안정성 고려



**enum 타입에 속성을 넣어줄 수 있음.**

```java
public enum Grade {
    BASIC(10), GOLD(20), DIAMOND(30);
    
    private final int discountPercent;
    
    Grade(int discountPercent) {
        this.discountPercent = discountPercent;
    }
}
```

(private) 생성자로 선언할 때 값을 주입해서 속성을 부여할 수 있음.

여기서 등급에 따라 discountPercent가 달라지므로 Grade에 속성을 부여하는 것이 타당함. 



```java
public enum HttpStatus {
    
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
    
    private final int code;
    private final String message;
    
    HttpStatus(int code, int message) {
        this.code = code;
        this.message = message;
    }
}
```

Http 상태 코드도 code와 message 필드를 갖게하여 enum 타입 자체가 속성을 갖게할 수 있음.