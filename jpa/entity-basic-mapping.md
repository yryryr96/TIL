### 객체와 테이블 매핑



#### @Entity

- @Entity가 붙은 클래스는 JPA가 관리하고, 엔티티라 한다.
- JPA를 사용해서 테이블과 매핑할 클래스는 @Entity가 필수이다.

- **주의**
  - 기본 생성자가 필수이다. (public, protected)
  - final 클래스, enum, interface, inner 클래스 사용 x
  - 저장할 필드에 final 사용 x



#### @Table

- @Table은 엔티티와 매핑할 테이블 지정

- name 속성을 사용해서 매핑할 테이블 이름을 변경할 수 있다. (`@Table(name = "orders")`)



#### DB 스키마 자동 생성

`hibernate.hbm2ddl.auto`

- `create` : 기존 테이블 삭제 후 다시 생성 (DROP + CREATE)
- `create-drop` : create와 같으나 종료 시점에 테이블 DROP
- `update` : 변경분만 반영 (운영 DB에 사용하면 안된다.)
- `validate` : 엔티티와 테이블이 정상 매핑되었는지만 확인
- `none` : 사용하지 않음



###### 주의

- 개발 초기 단계에는 `create` or `update`
- 테스트 서버는 `update` or `validate`
- 스테이징, 운영 서버는 `validate` or `none`



### 필드와 컬럼 매핑



#### 매핑 어노테이션 정리

- `Column` : 컬럼 매핑
  - insertable, updatable
  - nullable(DDL)
  - unique(DDL)
  - columnDefinition(DDL)
  - length(DDL)
  - precision, scale(DDL)
- `@Temporal` : 날짜 타입 매핑
  - **LocalDate, LocalDateTime 사용할 때는 생략 가능** (대부분 이것을 사용)
  - TemporalType.DATE : 날짜, DB date 타입과 매핑
  - TemporalType.TIME : 시간, DB time 타입과 매핑
  - TemporalType.TIMESTAMP : 날짜와 시간, DB timestamp 타입과 매핑
- `Enumerated` : enum 타입 매핑
  - `EnumType.ORDINAL` : enum class에 정의된 순서를 DB 에 저장 (사용 X)
  - `EnumType.STRING` : enum 이름을 DB에 저장
- `@Lob` : BLOB, CLOB 매핑
  - 매핑하는 필드 타입이 문자면 `CLOB`, 나머지는 `BLOB`
  - `CLOB` : String, char[], java.sql.CLOB
  - `BLOB` : byte[], java.sql.BLOB
- `@Transient` : 특정 필드를 컬럼에 매핑하지 않음. (DB 테이블에 추가하지 않을 필드)
  - 필드 매핑 x, DB 저장 x, 조회 x
  - 주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 때 사용



### 기본 키 매핑



- 직접 할당 : `@Id` 만 사용

- 자동 생성(`@GeneratedValue`)

  - `IDENTITY`: DB에 위임, MYSQL

    ```
    - 영속성 컨텍스트 1차 캐시에는 pk 정보가 필요하다.
    - JPA 는 쓰기 지연 SQL 저장소에 SQL을 저장했다가 commit 시점에 DB에 적용한다.
    - Auto Increment 전략은 DB에 데이터가 저장되어야 pk를 알 수 있다.
    - 딜레마 발생 -> IDENTITY 전략에서는 em.persist 시점에 SQL을 DB에 바로 전달
    ```

  - `SEQUENCE` : DB 시퀀스 오브젝트 사용, ORACLE (@SequenceGenerator 필요)

    - DB에서 시퀀스 오브젝트를 사용해서 pk를 가져오고, 인스턴스 id에 적용
    - 따라서, 쓰기 지연 기능을 사용할 수 있다.
    - 항상 DB에 접근해서 pk를 가져오기 때문에 성능 문제가 발생할 수 있다.
    - `allocationSize` : DB에서 한번에 allocationSize 만큼 sequence를 할당
      - ex ) allocationSize = 50
      - 애플리케이션이 DB 시퀀스에 접근하면 DB 시퀀스 값은 allocationSize만큼 증가
      - 애플리케이션은 pk 시작 값부터 DB 시퀀스 값까지 메모리로 pk 적용 -> DB 접근 x
      - pk가 증가하다가 DB 시퀀스 값에 도달하면 다시 DB 시퀀스에 접근해서 시퀀스 갱신

  - `TABLE` : 키 생성용 테이블 사용, 모든 DB에 사용

  - `AUTO`: 방언에 따라 자동 지정, 기본값