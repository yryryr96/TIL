### @Conditional

`Condition` 인터페이스 구현체의 `matches` 메서드 반환 값이 `true` 일 때 해당 설정 정보가 정상적으로 적용된다.



##### Condition 구현체

```java
public class MemoryCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String memory = context.getEnvironment().getProperty("memory");
        return "on".equals(memory);
    }
}
```

- `ConditionContext` : 스프링 컨테이너, 환경 정보등을 담고 있다.
- `AnnotatedTypeMetadata` : 애노테이션 메타 정보를 담고 있다.

`Condition` 인터페이스를 구현해서 자바 시스템 속성이 `memory=on` 이라고 되어 있을 때만 메모리 기능이 동작하도록 구현

```
#VM Options
#java -Dmemory=on -jar project.jar
```



##### MemoryConfig

```java
@Configuration
@Condition(MemoryCondition.class)
public class MemoryConfig {
    
    @Bean
    public MemoryController memoryController() {
        return new MemoryController(memoryFinder());
    }
    
    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }
}
```

설정 정보를 등록할 때 `@Condition`에 등록된 `Condition` 구현체의 `matches` 메서드 반환 값이 `true`이면 해당 설정 정보는 정상적으로 등록되고, `false`라면 해당 설정은 무효화된다.

즉, `@Condition` 은 `if문` 이라고 생각하면 편하다.



### @ConditionalOnXxx

스프링은 `@Conditional`과 관련해서 개발자가 편리하게 사용할 수 있도록 수 많은 `@ConditionalOnXxx` 를 제공한다.



- `@ConditionalOnClass`, `@ConditionalOnMissingClass`
  - 클래스가 있는 경우 동작, 나머지는 반대
- `@ConditionalOnBean`, `@ConditionalOnMissingBean`
  - 빈이 등록되어 있는 경우 동작, 나머지는 반대
- `@ConditionalOnProperty`
  - 환경 정보가 있는 경우 동작한다.
- `@ConditionalOnResource`
  - 리소스가 있는 경우 동작한다.
- `@ConditionalOnWebApplication`, `@ConditionalOnNotWebApplication`
  - 웹 애플리케이션인 경우 동작한다.
- `@ConditionalOnExpression`
  - SpEL 표현식에 맞는경우 동작한다.



### @AutoConfiguration

자동 구성을 사용하기 위해 적용되는 애노테이션



##### MemoryAutoConfig

```java
@AutoConfiguration
@ConditionalOnProperty(
    name = {"memory"},
    havingValue = "on"
)
public class MemoryAutoConfig {
    public MemoryAutoConfig() {
    }

    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }

    @Bean
    public MemoryController memoryController() {
        return new MemoryController(this.memoryFinder());
    }
}
```

- 자동 구성을 사용할 설정 정보
- `@ConditionalOnProperty` : 환경 정보의 memory 값이 `on` 일 때 적용



##### resources/META-INF/spring/ org.springframework.boot.autoconfigure.AutoConfiguration.imports

```
memory.MemoryAutoConfig
```

자동 구성을 사용할 클래스 표시



위와 같이 작성한 라이브러리는 `build.gradle`에서 import만 해주면 스프링 부트에서 인식해서 자동으로 설정 정보를 적용한다.



##### ImportSelector

```java
public interface ImportSelector {
    String[] selectImports(AnnotationMetadata importingClassMetadata);
}
```

`@Import` 애노테이션에 `ImportSelector` 구현체를 넣으면 내부적으로 `selectImports` 메서드를 실행하고 반환 받은 설정 클래스를 적용한다.



**즉, `@Import` 에 클래스를 명시적으로 작성하면 정적으로 구성 정보를 적용하는 것이고, `ImportSelector` 구현체를 작성하면 구성 정보를 동적으로 적용할 수 있다.**



##### @EnableAutoConfiguration

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {

	String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

	Class<?>[] exclude() default {};

	String[] excludeName() default {};

}
```

- `@Import` 내부에 `ImportSelector` 구현체가 들어가 있다. 즉, 동적으로 구성 정보를 적용한다.
- `AutoConfigurationImportSelector`의 `selectImports` 메서드에서 내부적으로 `resources/META-INF/spring/ org.springframework.boot.autoconfigure.AutoConfiguration.imports` 경로에 작성된 메서드를 String 배열에 담아서 반환한다.
- 반환 받은 구성 정보들을 적용한다.



##### SpringBoot AutoConfigure 과정

- `@SpringBootApplication` -> `@EnableAutoConfiguration` -> `@Import(AutoConfigurationImportSelector.class)` -> `resources/META-INF/spring/ org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일에 작성된 자동 구성 정보 선택 및 적용