# 2024-05-19 TIL



### String Class

`String` 은 기본적으로 `immutable`

```java
String str1 = new String("hello");
String str2 = new String("hello");

// 동등성, 동일성 비교
System.out.println(str1 == str2); // false
System.out.println(str1.equals(str2)); // true
```

str1과 str2는 서로 다른 인스턴스를 생성한 것이므로 서로 다른 참조 값을 가짐. 따라서, str1과 str2는 동일성은 false, 동등성은 내용 비교이므로 true



```java
String str1 = "hello";
String str2 = "hello";

// 동등성, 동일성 비교
System.out.println(str1 == str2); // true
System.out.println(str1.equals(str2)); // true;
```

`String` 은 `immutable` 객체이기 때문에 동일성 비교에서 false가 나올 것으로 예상했다. 하지만, 동일성 비교도 true가 나옴

**why???**

**문자열 리터럴을 사용하는 경우** 자바는 메모리 효율과 성능 최적화를 위해 `String Pool`을 사용

자바가 실행되는 시점에 문자열 리터럴이 있으면 String Pool에 `String` 인스턴스를 미리 만들어둠.

문자열 리터럴이 String Pool에 있으면 그 String 인스턴스를 참조함. 즉, 위와 같은 상황에서는 

1. 컴파일할 때 String Pool에 "hello" 값을 가진 String 인스턴스를 저장.
2. str1, str2가 초기화될 때 String Pool에서 "hello"라는 값을 가진 String 인스턴스를 조회하고 그것을 참조.

**즉, str1과 str2는 같은 인스턴스를 참조한다. 따라서, 동일성 비교에서도 true가 반환되는 것.**

```
String Pool은 힙 영역을 사용함.
String Pool에서 문자열을 찾을 때는 해시 알고리즘을 사용하기 때문에 매우 빠르게 String 인스턴스를 찾을 수 있음.
```



=> String 인스턴스가 문자열 리터럴로 생성된 것인지 `new String()`으로 생성된 것인지 알 수 없음.  따라서, 문자열을 비교할 때는 동등성을 비교하는 `equals()` 메서드로 비교하는 것이 바람직하다.
