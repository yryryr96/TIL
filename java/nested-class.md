# 2024.05.24 TIL



### final

final 키워드는 변수, 메소드, 클래스에 사용할 수 있음.

- **변수** : 상수로서 선언된 값의 변경을 금지
- **메소드** : 오버라이딩 금지
- **클래스** : 상속 금지

불변성 보장?? -> 상황에 따라서 생각해봐야 함.



### 중첩 클래스

- `정적 중첩 클래스` : 외부 클래스 내부에 static으로 선언된 클래스, 외부 클래스의 인스턴스에 소속되지 않음.
  - static 클래스이므로 인스턴스 멤버에 접근할 수 없음.
  - 외부 클래스와 무관한 클래스
  - 외부 클래스의 인스턴스에 소속되지 않기 때문에 인스턴스 없이 생성 가능.
    - `ex) Outer.StaticNested nested = new Outer.StaticNested();`

- `내부 클래스` : 외부 클래스 내부에 static 없이 선언된 클래스, 외부 클래스의 인스턴스에 소속됨.
  - **내부 클래스** : 외부 클래스 인스턴스 멤버에 접근 가능
  - **지역 클래스** : 내부 클래스 특징 + 지역 변수에 접근
  - **익명 클래스** : 지역 클래스 특징 + 클래스의 이름이 없는 특별한 클래스
  - 내부 클래스는 외부 클래스의 참조 값을 가지고 있기 때문에 인스턴스 멤버에 접근이 가능하다.
  - 내부 클래스는 외부 클래스의 인스턴스에 소속되기 때문에 생성하기 위해서는 인스턴스가 필요함.
    - `ex) Outer.Inner inner = outer.new Inner();`

```java
class Outer {
    
    // 정적 중첩 클래스
    static class StaticNested {}
    
    // 내부 클래스
    class Inner {}
}
```



**| 정리**

- `중첩 클래스` : 정적 중첩 클래스 + 내부 클래스
- 중첩 클래스를 사용하고 인스턴스 멤버에 접근하려면 `내부 클래스` 사용 고려
- 중첩 클래스를 사용하고 인스턴스 멤버에 접근할 일이 없으면 `정적 중첩 클래스` 사용 고려

- **중첩 클래스는 특정 클래스가 다른 하나의 클래스 안에서만 사용되거나, 둘이 아주 긴밀하게 연결되어 있는 특별한 경우에만 사용해야 함.** -> 이렇지 않은 경우에는 따로 클래스를 빼야함.

- `논리적 그룹화` : 특정 클래스가 하나의 클래스 안에서만 사용될 경우 해당 클래스 안에 포함되는 것이 논리적으로 그룹화 가능. -> 패키지를 열었을 때 다른 곳에서 사용되지 않는 클래스가 외부로 노출되는 것을 방지할 수 있음.
- `캡슐화` : 중첩 클래스는 외부 클래스의 private 멤버에 접근할 수 있다. 이렇게 함으로써 둘을 긴밀하게 연결하고 멤버에 접근하기 위한 getter와 같은 public 메서드를 제거할 수 있음. (Car, Engine)



### 지역 클래스

`지역 클래스`는 기본적으로 내부 클래스의 특징 + 지역 변수에 접근 가능하다. 따라서, 인스턴스 멤버에도 당연히 접근 가능하다.

```java
public class LocalOuter {
    private int outInstanceVar = 3;
    
    public Printer process(int paramVar) {
        
        int localVar = 1;
        
        class LocalPrinter implements Printer {
            
            int value = 0;
            
            @Override
            public void print() {
                System.out.println("value = " + value);
                System.out.println("localVar = " + localVar);
                System.out.println("paramVar = " + paramVar);
                System.out.println("outInstanceVar = " + outInstanceVar);
            }
        
            
            LocalPrinter printer = new LocalPrinter();
            return printer;
    }
    
    public static void main(String[] args) {

        LocalOuter localOuter = new LocalOuter();
        Printer printer = localOuter.process(2);
        printer.print();

        // 추가
        System.out.println("필드 확인");
        Field[] fields = printer.getClass().getDeclaredFields();
        for (Field field : fields) {
            System.out.println("field = " + field);
        }
    }
}
```

```markdown
필드 확인
//인스턴스 변수
field = int nested.local.LocalOuter$1LocalPrinter.value
//캡처 변수
field = final int nested.local.LocalOuter$1LocalPrinter.val$localVar
field = final int nested.local.LocalOuter$1LocalPrinter.val$paramVar
//바깥 클래스 참조
field = final nested.local.LocalOuterV
nested.local.LocalOuter$1LocalPrinter.this$0
```



**| 생명 주기**

1. `static` : 클래스 로더가 클래스를 로딩한 시점부터 프로그램이 종료될 때까지 생존
2. `지역 변수` : 메서드가 실행되면 메서드 영역에 생성되고 메서드가 종료될 때 제거
3. `인스턴스` : heap 영역에 인스턴스가 생성되고 GC로 제거될 때까지 생존



위 코드 main 메서드 내부를 보면 LocalOuter 인스턴스가 생성되고, process 메서드가 실행되고 return 된다.

여기서, process 메서드 내부에 있는 지역 변수 localVar, paramVar은 process 메서드가 종료될 때 같이 메모리에서 제거된다. 하지만, LocalPrinter 인스턴스는 아직 참조하고 있는 곳이 있기 때문에 GC에 의해 제거되지 않는다. 따라서, LocalPrinter 인스턴스의 print 메서드를 실행하면 localVar, paramVar가 참조할 메모리 공간이 없으므로 값이 출력되지 않을 것 같지만 정상적으로 값이 출력된다. **그럼 LocalOuter 인스턴스의 print 메서드에 있는 localVar, paramVar은 어디를 참조하는것인가?**



**| 지역 변수 캡처**

사실 지역 클래스의 인스턴스를 생성할 때, 참조하고 있는 지역변수를 캡처해서 인스턴스에 복사하고 생성한다. 따라서, LocalPrinter 인스턴스의 print 메서드에서 가리키는 localVar, paramVar 변수는 process 메서드의 지역 변수가 아니라, 캡처되어 복사된 변수이다. 

`printer.getClass().getDeclaredFields()` 를 통해 LocalPrinter 인스턴스가 가지고 있는 인스턴스 멤버를 확인할 수 있다. 이를 통해 한 가지 더 확인할 수 있는데 지역 클래스는 외부 클래스의 참조 값을 가지고 있다고 했다. field를 보면 LocalOuter 클래스가 존재한다.



**| effectively final**

그럼 지역 변수 값을 중간에 바꾸면 어떻게 될까? 인스턴스에 복사한 값을 변경해야할까? 또는 인스턴스에서 캡처로 복사된 값을 변경하면 실제 지역 변수 값을 변경해야할까?

**이와 같은 동기화 문제는 매우 복잡하고, 사이드 이펙트가 발생할 수 있기 때문에 변경을 지역 변수는 변경 못하게 막아놨다. 즉, final을 붙여서 코드를 작성한 것과 결과가 동일하다.**

자바 이전 버전에서는 fianl을 붙여야 했지만, 현재는 붙이지 않아도 됨. 만약, 중간에 값을 변경하면 컴파일 오류가 발생한다. -> final을 붙이진 않았지만, 실제로는 붙인 것과 동일하게 작동하는 것을 실질적 final `effectivel final` 이라함.



### 익명 클래스

```java
// 인터페이스
public interface Process {
    void run();
}

// 메소드
public void hello(Process process) {
    process.run();
}

// 익명 클래스 사용
hello(new Process() {
    
    @Override
    public void run() {
        System.out.println("Anonymous.hello");
    }
}
```

- `new Process {body}`

익명 클래스는 본문을 정의하면서 동시에 생성한다.

`Process` 를 생성한 것이 아니라 Process의 인터페이스를 구현한 익명 클래스를 생성한 것.

body 부분에 Process 인터페이스를 구현한 코드를 작성하면 됨.



**| 장점**

익명 클래스를 사용하면 클래스를 별도로 정의하지 않고 인터페이스나 추상 클래스를 즉석에서 구현할 수 있어 코드가 더 간결해짐. 하지만, 복잡하거나 재사용이 필요한 경우에는 별도의 클래스를 정의하는 것이 좋음.

-> 상속 받는 클래스를 정의할 필요 없이 바로 구현해서 사용할 수 있음.