##### JDBC 표준 인터페이스

- JDBC(Java Database Connectivity)는 자바에서 데이터베이스에 접속할 수 있도록 하는 자바 API.
- JDBC는 데이터베이스에서 자료를 쿼리하거나 업데이트하는 방법을 제공한다.
- **Connection (연결), Statement (SQL 전달), ResultSet (결과 응답)**
- JDBC 인터페이스를 구현해서 MySQL, Oracle과 같은 DB Driver를 생성함.
- 즉, 사용자는 인터페이스에 의존해서 DB 종류에 관계 없이 접근할 수 있다. (각 드라이버는 DB 회사에서 직접 구현해서 제공)

- 따라서, 개발자는 DB에 따라 코드를 변경할 필요가 없다. Driver만 변경하면 됨.

- **한계**
  - DB마다 sql 사용법이 다르다.
  - 연결, SQL 전달, 결과 응답은 인터페이스로 해결이 가능하지만, sql은 DB에 맞게 사용해야 한다.



##### SQL Mapper

- 기존에는 애플리케이션 로직에서 바로 JDBC로 SQL을 전달했다. (애플리케이션 로직 -> JDBC)
- SQL Mapper를 사용하면 애플리케이션 로직에서 SQL Mapper로 전달하고 SQL Mapper에서 JDBC로 SQL을 전달한다. (애플리케이션 로직 -> SQL Mapper -> JDBC)

- **개발자가 SQL을 직접 작성해야 한다는 단점이 있다.**
- JdbcTemplate, MyBatis 등이 있다.



**ORM (Object Relational Mapping)**

- ORM은 객체를 관계형 데이터베이스 테이블과 매핑해주는 기술이다. 개발자가 반복적인 SQL을 직접 작성하지 않고, ORM 기술이 개발자 대신에 SQL을 동적으로 만들어 실행해준다.
- 각 DB 마다 다른 SQL을 사용하는 문제도 해결해준다.
- JPA는 자바 ORM 표준 인터페이스이고, 이것을 구현한 것으로 하이버네이트와 이클립스 링크 등이 있다.



**SQL Mapper vs ORM**

- 각각 장단점이 있다.
- SQL Mapper는 SQL만 직접 작성하면 나머지 번거로운 일은 SQL Mapper가 대신해준다.
- ORM 기술은 SQL 자체를 작성하지 않아도 되어서 개발 생산성이 매우 높아진다. 편리하지만 쉬운 기술은 아니므로 깊이있게 학습해야 한다.

이런 기술들도 내부적으로는 모두 JDBC를 사용하기 때문에 JDBC가 어떻게 동작하는지는 알아야 한다.



**JDBC 기본 작동 순서**

1. DriverManager가 Connection 획득 -> 라이브러리에서 H2, MySQL Driver를 찾아서 실행
2. sql 작성 후 connection의 Statement에 담아서 보냄
3. DB에서 sql에 따른 동작을 수행하고 ResultSet 반환
4. connection, statement, resultset 모두 사용하고 close() 해줘야함 -> 안해주면 리소스 누수가 발생한다.



##### Connection Pool

1. 애플리케이션 로직에서 Connection 조회
2. DB 드라이버가 DB와 TCP/IP 커넥션 연결
3. ID, PW 등 부가정보 전달
4. DB는 내부에서 인증 처리와, 세션을 생성
5. DB에서 드라이버에게 커넥션 생성이 완료됐다는 응답 전송
6. 드라이버 -> 애플리케이션 로직 (connection 반환)



DB에 접근하는 것은 커넥션 생성 시간 + sql 실행 시간, 리소스 등 무거운 작업이므로 사용자에게 좋지 않은 경험을 줄 수 있다.

이 문제를 해결하기 위해 `Connection Pool` 도입

- **애플리케이션이 시작하는 시점에 기본값으로 10개의 connection을 미리 생성해서 보관함.**

- 이렇게 하면 다음 DB 접근에서는 DB 드라이버를 통해 connection을 획득하는 것이 아니라 Connection Pool에서 미리 생성되어 있던 connection을 획득한다. (객체 참조)

- 모두 사용하고 connection을 종료하는 것이 아니라, connection을 다시 Connection Pool에 반환해야 함.



##### DataSource

connection을 얻는 방법이 여러 개가 있다. 

- DriverManager를 사용해서 새로운 connection을 생성해서 얻는 방법
- HikariCP 등 Connection Pool을 사용해서 connection을 얻는 방법

여기서 connection 얻는 방법을 변경한다면 애플리케이션 로직의 코드를 변경해야 한다. 이 문제를 해결하기 위해 자바는 **connection 획득 방법을 추상화**한 인터페이스 `DataSource` 를 제공한다.

```java
public interface DataSource {
    Connectino getConnection() throws SQLException;
}
```

```java
public class MemberRepository repository {
    
    private final DataSource dataSource;
    
    public MemberRepository (DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
   	private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
```

위와 같이 사용하면 MemberRepository는 `DataSource`에 의존하기 때문에 DataSource 즉, connection 획득 방법이 변경되어도 애플리케이션 로직 코드는 변경하지 않아도 동작한다.