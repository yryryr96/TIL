### JAR

- 자바는 여러 클래스와 리소스를 묶어서 `JAR`(Java Archive)라고 하는 압축 파일을 만들 수 있다.
- JAR 파일은 JVM 위에서 직접 실행되거나 또는 다른 곳에서 사용하는 라이브러리로 제공된다.
- 직접 실행하는 경우 `main()` 메서드가 필요하고, `MANIFEST.MF` 파일에 실행할 메인 메서드가 있는 클래스를 지정해두어야 한다.

```
Jar는 클래스와 관련 리소스를 압축한 단순한 파일이다. 필요한 경우 이 파일을 직접 실행할 수도 있고, 다른 곳에서 라이브러리로 사용할 수도 있다.
```



### WAR

- `WAR`(Web Application Archive) 파일은 웹 애플리케이션 서버(WAS)에 배포할 때 사용하는 파일이다.
- JAR 파일이 JVM 위에서 실행된다면, WAR는 웹 애플리케이션 서버 위에서 실행된다.
- 웹 애플리케이션 서버 위에서 실행되고, HTML 같은 정적 리소스와 클래스 파일을 모두 함께 포함하기 때문에 JAR와 비교해서 구조가 더 복잡하다.



##### WAR 구조

- WEB-INF
  - `classes` : 클래스 모음
  - `lib` : 라이브러리 모음
  - `web.xml` : 웹 서버 배치 설정 파일(생략 가능)
- `index.html` : 정적 리소스



- `WEB-INF` 폴더 하위는 자바 클래스와 라이브러리, 설정 정보가 들어가는 곳이다.
- `WEB-INF` 외 나머지 영역은 HTML, CSS 같은 정적 리소스가 사용되는 영역이다.



### Fat Jar

- 기본적으로 Jar 파일은 Jar 파일을 포함할 수 없다.
- 즉, 기본적인 Jar 파일은 라이브러리의 Jar 파일을 포함할 수 없다.
- **Jar 파일 내부에는 `class`들이 있다. 이 class를 모두 Jar 파일에 포함하는 방법이다.**
- 모든 클래스를 Jar 파일에 포함하기 때문에 용량이 커진다. -> FatJar



##### 장점

- Fat Jar 덕분에 하나의 Jar 파일에 필요한 라이브러리를 내장할 수 있게 되었다.
- 내장 톰캣 라이브러리를 Jar 내부에 내장할 수 있다.
- 하나의 Jar 파일로 배포, 웹 서버 설치 + 실행까지 모든 것을 단순화 할 수 있다.



##### 단점

- 모두 클래스로 포함되어 있기 때문에 어떤 라이브러리가 사용되었는지 확인하기 어렵다.

- 파일명 중복을 해결할 수 없다.
  - 파일명이 중복된다면 중복된 클래스나, 리소스 중 하나를 포기해야 한다.



### 실행 가능 Jar

스프링 부트는 기존 `Jar`, `Fat Jar` 의 문제를 해결하기 위해 jar 내부에 jar를 포함할 수 있는 특별한 구조의 jar를 만들고 동시에 만든 jar를 내부 jar를 포함해서 실행할 수 있게 했다. 이것을 `Executable Jar(실행 가능 Jar)`라 한다.



- jar 내부에 jar를 포함하기 때문에 어떤 라이브러리가 포함되어 있는지 쉽게 확인 가능하다.
- jar 내부에 jar를 포함하기 때문에 같은 이름 파일이 있어도 모두 인식 가능하다. `a.jar`, `b.jar` 개별 인식



##### 구조

- `boot-0.0.1-SNAPSHOT.jar`
  - `META-INF`
    - `MANIFEST.MF`
  - `org/springframework/boot/loader`
    - `JarLauncher.class` : 스프링 부트 `main()` 실행 클래스
  - `BOOT-INF`
    - `classes` : 개발한 class 파일과 리소스 파일
    - `lib` : 외부 라이브러리
  - `classpath.idx` : 외부 라이브러리 모음
  - `layers.idx` : 스프링 부트 구조 정보



##### Jar 실행 정보

`java -jar xxx.jar`를 실행하면 `MANIFEST.MF` 를 찾고 `Main-Class`를 읽어 `main()`메서드를 실행한다.



##### 스프링 부트가 만든 MANIFEST.MF

```
Manifest-Version: 1.0
Main-Class: org.springframework.boot.loader.JarLauncher
Start-Class: hello.boot.BootApplication
Spring-Boot-Version: 3.0.2
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
Spring-Boot-Classpath-Index: BOOT-INF/classpath.idx
Spring-Boot-Layers-Index: BOOT-INF/layers.idx
Build-Jdk-Spec: 17
```

- `Main-Class`
  - `hello.boot.BootApplication`이 아니라 `JarLauncher`라는 클래스를 실행하고 있다.
  - `JarLauncher`는 스프링 부트가 빌드시에 넣어준다.
  - `JarLauncher`는 스프링 부트가 jar 내부에 jar를 읽어 들일 수 있게 하고, 특별한 구조에 맞게 클래스 정보도 읽어들인다. 그 후, `Start-Class`에 지정된 `main()`을 호출한다.



##### 실행 과정 정리

1. java -jar xxx.jar

2. MANIFEST.MF 인식

3. JarLauncher.main() 실행

   - `BOOT-INF/classes/` 인식

   - `BOOT-INF/lib/` 인식

4. `BootApplication.main()` 실행