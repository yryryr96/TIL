# 2024-05-20 TIL



### StringBuilder

기본적으로 String 클래스는 immutable. 그래서 String 인스턴스의 값을 변경하면 항상 새로운 인스턴스를 생성함. 

이는 메모리 낭비가 될 수 있음. 따라서, 문자열 조작이 많은 상황에서는 mutable한 StringBuilder를 사용하는 것이 메모리, 효율 측면에서 이득.



**자바는 String 연산을 최적화 해줌.**

```java
String result = str1 + str2;

// 위와 같은 연산을 아래와 같이 최적화. -> 메모리 낭비를 막기 위해
String result = new StringBuilder().append(str1).append(str2).toString();
```



```java
long startTime = System.currentTimeMillis();
String result = "";
for (int i = 0; i < 100000; i++) {
    result += "Hello ";
}
long endTime = System.currentTimeMillis();
System.out.println(result);
System.out.println(endTime - startTime + "ms"); // 5579ms
```

**위와 같은 상황에 너무 오래걸림. why?**

```java
for (int i = 0; i < 100000; i++) {
    result += "Hello ";
}

// 위의 부분이 사실 아래와 같이 수행되고 있음.
for (int i = 0; i < 100000; i++) {
    result = new StringBuilder().append(result).append("Hello ").toString();
}
```

즉, 10만번의 StringBuilder 인스턴스와 String 인스턴스를 생성했을 것임..



```java
long startTime = System.currentTimeMillis();
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 100000; i++) {
    sb.append("Hello ");
}
long endTime = System.currentTimeMillis();

System.out.println("result = " + sb.toString());
System.out.println(endTime - startTime + "ms"); // 7ms
```

처음에 StringBuilder 인스턴스를 하나만 생성해주고, for문에서 연산만 수행해준 다음 마지막에 String 인스턴스를 한 번만 생성해주는 식으로 바꿔주면 수행 시간이 매우매우매우 줄어든 것을 확인할 수 있음. -> 문자열 연산이 많은 경우에는 StringBuilder를 사용하는 것이 좋다.



**| 정리**

**문자열을 합칠 때 대부분의 경우는 최적화가 되므로 + 연산으로 충분.**

But

1. 반복문에서 반복해서 문자열을 조작하는 경우
2. 조건문을 통해 동적으로 문자열을 조합하는 경우
3. 복잡한 문자열의 특정 부분을 변경하는 경우
4. 매우 긴 대용량 문자열을 다루는 경우

**위와 같은 경우에는 대체적으로 StringBuilder를 사용하는 것이 이득이다.**

