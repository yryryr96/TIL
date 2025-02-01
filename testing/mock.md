### Test Double

- `Dummy` : 아무 것도 하지 않는 깡통 객체
- `Fake` : 단순한 형태로 동일한 기능을 수행하나, 프로덕션에서 쓰기에는 부족한 객체 `FakeRepository`
- `Stub` : 테스트에서 요청한 것에 대해 미리 준비한 결과를 제공하는 객체, 그 외에는 응답하지 않는다.
- `Spy` : Stub이면서 호출된 내용을 기록하여 보여줄 수 있는 객체. 일부는 실제 객체처럼 동작시키고 일부만 Stubbing 할 수 있다.
- `Mock` : 행위에 대한 기대를 명세하고, 그에 다라 동작하도록 만들어진 객체



#### Stub vs Mock

- `Stub` : 상태 검증
- `Mock` : 행위 검증

Martin Fowler가 작성한 글에서 Stub, Mock에 대한 예제를 확인할 수 있다.

https://martinfowler.com/articles/mocksArentStubs.html



##### Stub

```java
public interface MailService {
  public void send (Message msg);
}
public class MailServiceStub implements MailService {
  private List<Message> messages = new ArrayList<Message>();
  public void send (Message msg) {
    messages.add(msg);
  }
  public int numberSent() {
    return messages.size();
  }
}                                 
```

  ```java
  public void testOrderSendsMailIfUnfilled() {
      Order order = new Order(TALISKER, 51);
      MailServiceStub mailer = new MailServiceStub();
      order.setMailer(mailer);
      order.fill(warehouse);
      assertEquals(1, mailer.numberSent());
    }
  ```

mailer의 상태 `mailer의 messages의 크기`를 검증한다.



##### Mock

```java
public void testOrderSendsMailIfUnfilled() {
    Order order = new Order(TALISKER, 51);
    Mock warehouse = mock(Warehouse.class);
    Mock mailer = mock(MailService.class);
    order.setMailer((MailService) mailer.proxy());

    mailer.expects(once()).method("send");
    warehouse.expects(once()).method("hasInventory")
      .withAnyArguments()
      .will(returnValue(false));

    order.fill((Warehouse) warehouse.proxy());
}
```

mailer의 `send` 메서드가 1번 호출됐다는 **행위**를 검증하고 있다.



### Mockito

```java
@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private MailSendClient mailSendClient;

    @Mock
    private MailSendHistoryRepository mailSendHistoryRepository;

    @InjectMocks
    private MailService mailService;

    @DisplayName("메일 전송 테스트")
    @Test
    void sendMail() throws Exception {
        //given
        when(mailSendClient.sendEmail(anyString(), anyString(), anyString(), anyString())).thenReturn(true);


        //when
        boolean result = mailService.sendMail("", "", "", "");

        //then
        assertThat(result).isTrue();
        // save라는 행위가 1번 불렸는지 검증
        verify(mailSendHistoryRepository, times(1)).save(any(MailSendHistory.class));
    }

}
```

- `@ExtendWith(MockitoExtension.class)` : JUnit에서 Mockito 기능을 확장하여 사용할 수 있도록 해주는 어노테이션
  - @Mock, @InjectMocks 등의 Mockito 어노테이션이 자동으로 동작
- `@Mock` : 테스트에서 사용할 가짜(Mock) 객체를 생성하는 어노테이션

- `@InjectMocks` : @Mock으로 생성한 Mock 객체들을 테스트 대상 클래스 `MailService`에 자동으로 주입해주는 어노테이션
  - MailService 생성에는 `MailSendClient, MailSendHistoryRepository`가 필요하고 이 클래스가 Mock 객체로 존재할 때, MailService에 Mock 객체를 주입해서 객체를 생성한다.

- `@Spy` : 한 클래스에 존재하는 여러 메서드 중 특정 메서드만 stubbing 해서 사용하고 싶은 경우 사용하는 어노테이션
  - a, b, c 메서드가 존재하고 a, b 메서드는 본 객체의 로직을 그대로 사용하고 싶고, c 메서드만 stubbing 해서 사용하고 싶은 경우



### BDDMockito

```java
@DisplayName("메일 전송 테스트")
@Test
void sendMail() throws Exception {
    //given
    given(mailSendClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
            .willReturn(true);

    //when
    boolean result = mailService.sendMail("", "", "", "");

    //then
    assertThat(result).isTrue();
    // save라는 행위가 1번 불렸는지 검증
    verify(mailSendHistoryRepository, times(1)).save(any(MailSendHistory.class));
}
```

기존 Mockito를 상속받아 `when, thenReturn`을 BDD 테스트 방식에 맞게 `given, willReturn` 형식으로 변경한 것

모든 기능은 Mockito와 동일하다.



### 정리

단위 테스트로 검증된 여러 로직을 Mock 객체로 대신하는 경우, 실제 프로덕션 코드의 런타임 시점에 일어날 일을 정확하게 Stubbing 했다고 단언할 수 없다. -> 실제 런타임에서 어떤 오류가 발생할지 모른다.

따라서, 외부 시스템이나 정확하게 Stubbing 할 수 있는 객체들에 Mock을 적용하고 비용을 조금 더 들여서 실제 객체를 가지고 통합 테스트를 진행하는 것이 더 낫지 않나 생각한다.