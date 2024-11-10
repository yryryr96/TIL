# Redis Repository - template



### Redis Entity

```java
@Getter
@RedisHash(value = "refresh", timeToLive = 5)
public class RedisRefreshToken {

    @Id @Indexed
    private String authId;

    @Indexed
    private String token;

    private String role;

    public RedisRefreshToken(String token, String role) {
        this.token = token;
        this.role = role;
    }
}
```

- @RedisHash를 명시함으로써 redis에서 사용할 Entity임을 선언
  - redis에는 @RedisHash의 value:field 값으로 key, field의 값이 value로 저장된다.
- RedisRepository에서 findBy---와 같이 메서드를 사용하고 싶다면 필드에 **@Indexed**를 선언해야 한다.
  - @Indexed가 붙은 필드들은 내부적으로 인덱스가 생성되어 redis에 추가적인 key가 생성된다. ttl이 만료된 entity가 지워질 때 같이 지워질 것 같지만 지워지지 않는다. 이를 해결하기 위해선 `RedisConfig`에 `@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)` 설정을 추가해서 해결 가능하다.



### Redis Repository

```java
public interface RefreshTokenRepository extends CrudRepository<RedisRefreshToken, String> {
    Optional<RedisRefreshToken> findByToken(String token);

    Optional<RedisRefreshToken> findByAuthId(String authId);

    Optional<RedisRefreshToken> findByAuthIdAndToken(String authId, String token);   
}
```

- redisRepository는 CrudRepository를 상속 받아 구현한다. JpaRepository 사용하는 것과 비슷하다.
- redis Entity에서 @Indexed 애노테이션이 붙은 필드로 데이터 조작이 가능하다.



### RedisRepositoryTest

```java
@SpringBootTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository repository;

    @Test
    void test() throws Exception {

        repository.deleteAll();
        //given
        RedisRefreshToken refreshToken = new RedisRefreshToken("tttttaaaaaa", "ROLE_USER");

        //when
        repository.save(refreshToken);

        RedisRefreshToken result1 = repository.findByAuthId(refreshToken.getAuthId()).get();
        RedisRefreshToken result2 = repository.findByToken(refreshToken.getToken()).get();
        RedisRefreshToken result3 = repository.findByAuthIdAndToken(refreshToken.getAuthId(), refreshToken.getToken()).get();

        System.out.println(result1.getAuthId());
        System.out.println(result2.getToken());

        //then
        assertThat(repository.count()).isEqualTo(1);
        assertThat(result1.getToken()).isEqualTo(result2.getToken());
        assertThat(result3.getToken()).isEqualTo(result2.getToken());
        assertThat(result1.getAuthId()).isEqualTo(result2.getAuthId());
        assertThat(result3.getAuthId()).isEqualTo(result2.getAuthId());
    }
}
```



### RedisTemplate

RedisTemplate을 사용하면 특정 Entity 외에 여러가지 원하는 타입을 넣을 수 있다. (ex : String, Set, Hash 등)

template을 선언하고 원하는 타입에 맞는 Operations를 꺼내 사용한다.



##### RedisConfig에 RedisTempalte 빈 등록

```java
@Configuration
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
}
```



##### RedisTemplateTest

```java
@SpringBootTest
public class RedisTemplateTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void testStrings() throws Exception {

        //given
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String key = "stringKey";

        //when
        valueOperations.set(key, "hello");

        //then
        String value = valueOperations.get(key);
        assertThat(value).isEqualTo("hello");
    }

    @Test
    void testSet() throws Exception {

        //given
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        String key = "setKey";

        //when
        setOperations.add(key, "h", "e", "l", "l", "o");

        //then
        Set<String> members = setOperations.members(key);
        Long size = setOperations.size(key);

        assertThat(members).containsOnly("h", "e", "l", "o");
        assertThat(size).isEqualTo(4);
    }

    @Test
    void testHash() throws Exception {

        //given
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        String key = "hashKey";
        //when
        hashOperations.put(key, "hello", "world");

        //then
        Object value = hashOperations.get(key, "hello");
        assertThat(value).isEqualTo("world");

        Map<Object, Object> entries = hashOperations.entries(key);
        assertThat(entries.keySet()).containsOnly("hello");
        assertThat(entries.values()).containsOnly("world");

        Long size = hashOperations.size(key);
        assertThat(size).isEqualTo(1);
        assertThat(entries.size()).isEqualTo(1);
    }
}
```



### redis-cli 명령어

- ##### String

  - set <key> <value>
  - get <key>
  - del <key>

- **Set**
  - SMEMBERS <key> : key 내용 확인
  - SISMEMBER <key>  <member> : 특정 member가 Set에 존재하는지 확인
  - SCARD <key> : Set의 멤버 수 반환
  - SPOP <key> : Set에서 임의의 멤버를 제거하고 반환

- ##### Hash (자바의 HashMap이라고 생각 field, value 형태를 가지고 있음)

  - HGETALL <key> : Hash에 존재하는 모든 내용 표시
  - HGET <key> <field> : key에 해당하는 hash에서 field의 value를 반환
  - HSET <key> <field> <value> : key에 해당하는 hash에 field, value 설정
  - HDEL <key> <field> : key에 해당하는 hash에서 특정 field 삭제
  - HLEN <key> : key에 해당하는 hash에 저장된 필드 개수 반환
  - HKEYS <key> : key에 해당하는 hash의 모든 field 반환
  - HVALS <key> : key에 해당하는 hash의 모든 value 반환