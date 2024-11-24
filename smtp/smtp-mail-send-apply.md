# SMTP (Simple Mail Transfer Protocol)

`SMTP`는  간이 전자 우편 전송 프로토콜의 약자로 인터넷에서 이메일을 전송하는데 사용되는 표준 프로토콜이다. 메일 클라이언트가 수신 서버로 메시지를 발송하거나 포워드 할 수 있도록 지원한다. 마찬가지로 발신자는 SMTP 서버에 이메일을 발송하도록 지시할 수 있다.



```sh
Google SMTP Server를 이용해서 회원가입 할 때 메일을 통한 인증 절차를 구현하기 앞서 메일 전송 기능을 구현해보고자 한다.
```



### 준비물

- **Google 계정**
  - 2차 보안 완료한 계정
- `java-mail` 라이브러리



#### 흐름

1. 사용자에게 메일을 보낼 계정(Google 계정)을 준비한다.
2. Spring 애플리케이션에서 `JavaMailSender` 를 사용해서 사용자에게 메일을 보낸다.



### 구글 계정 설정

1. Google 계정 관리 접속
2. `보안` 탭에 2단계 인증이 완료되었는지 확인
3. 검색 탭에 `앱 비밀번호` 검색
4. `앱 비밀번호` 생성 및 비밀번호 **기억**



### Spring 애플리케이션 설정

1. **`build.gradle`에 java mail 라이브러리 추가**

   - `implementation 'org.springframework.boot:spring-boot-starter-mail'`

2. **`application.yml`에 mail 설정 추가**

   ```yaml
   spring:
     mail:
       host: smtp.gmail.com
       port: 587
       username: <google email>
       password: <앱 비밀번호>
       properties:
         mail.smtp:
           auth: true
           starttls:
             enable: true
   ```

   - `spring.mail.host` : 이메일을 보낼 SMTP 서버의 호스트 주소
   - `spring.mail.port` : SMTP 서버에서 이메일을 보낼 때 사용하는 포트 번호
     - `587` : TLS 전용 포트, 암호화 전송 지원
     - `465` : SSL 전용 포트
   - `spring.mail.username` : Google 계정 이메일 주소, 발신자 이메일
   - `spring.mail.password` : SMTP 인증 시 사용하는 비밀번호 (앱 비밀번호)
   - `spring.mail.properties.mail.smtp.auth` : SMTP 서버에서 인증을 요구하는지 설정, Gmail은 인증이 필수이므로 true
   - `spring.mail.properties.mail.smtp.starttls.enable` : STARTTLS(암호화된 SMTP 통신) 사용 여부 설정

3. **`JavaMailSender` 사용**

##### MailDto

```java
@Getter @Setter
@NoArgsConstructor
public class MailDto {

    private String address;
    private String title;
    private String message;
}
```

##### MailService

```java
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String FROM_ADDRESS;

    public void mailSend(MailDto mailDto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailDto.getAddress());
        message.setFrom(FROM_ADDRESS);
        message.setSubject(mailDto.getTitle());
        message.setText(mailDto.getMessage());

        mailSender.send(message);
    }
}
```

- SimpleMailMessage
  - `setTo()` : 받는 사람 주소
  - `setFrom()` : 보내는 사람 주소
  - `setSubject()` : 제목
  - `setText()` : 내용
- `mailSender.send(message)` : 실제 메일 발송 부분



### 트러블 슈팅

JavaMailSender를 사용하기 위해서는 application.yml에 값들을 할당해서 사용했다. 

local에서 기능이 정상적으로 작동하는지 확인하고 Github Repository에 push 후 Github Actions 에서 빌드하는 과정에서 `BeanDefinitionException`이 발생했다. 

로그를 추적해보니` JavaMailSender`가 빈으로 주입이 되지 않는 것을 확인했다. 결과적으로 `application.yml`이 Github Actions 스크립트에서 생성되지 않는 것이 문제였다.

나의 레포지토리 최상위 폴더에는 다양한 폴더가 있기 때문에 Github Actions에서 service를 matrix로 관리하고 있었다. service에 접근해서 `yml` 파일을 생성하는 경로를 `./src/main/resources`로 설정했는데 이 때 **기준이 되는 위치는 repository의 최상위 폴더**이기 때문에 정상적인 경로에 application.yml이 설치되지 않아서 문제가 발생했다.

경로를 `./${{ matrix.service }}/src/main/resources` 고치니까 해결되었다.



이제 메일 전송 기능은 확인했고, 인증 코드를 전송하고 일치하는지 확인해서 가입 절차가 완료되도록 구현해보겠다.