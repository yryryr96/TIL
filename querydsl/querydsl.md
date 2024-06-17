#### JPAQueryFactory

QueryDsl을 사용하기 위해 필요하다. 파라미터로 EntityManager를 넘긴다.

```java
JPAQueryFactory queryFactory = new JPAQueryFactory(em);
```



#### QueryDsl 기본 문법

생성한 JPAQueryFactory와 Qtype을 사용해서 SQL을 작성할 수 있다.

```java
JPAQueryFactory queryFactory = new JPAQueryFactory(em);

// 나이가 10살 보다 많고, 이름이 example인 Member 조회
List<Member> result = queryFactory
                        .select(QMember.member)
                        .from(QMember.member)
                        .where(QMember.member.username.eq("example"),
                              QMember.member.age.gt(10))
                        .offset(1)
                        .limit(2)
                        .fetch()
```

### 

### 서브 쿼리

`JPAExpressions`를 사용해서 서브쿼리 사용이 가능하다. 서브 쿼리에 들어갈 Qtype은 주 쿼리의 Qtype과 달라야 한다.

```java
// member = QMember.member

// 서브쿼리에 들어갈 Qtype 생성
QMember memberSub = new QMember("memberSub");

// Member 중에서 가장 나이가 많은 멤버 조회
List<Member> result = queryFactory
    .selectFrom(member)
    .where(member.age.eq(
    		JPAExpressions
    			.select(memberSub.age.max())
    			.from(memberSub)
    ))
    .fetch();
```



### 프로젝션

##### 프로젝션 대상이 하나

```java
List<String> result = queryFactory
                        .select(member.username)
                        .from(member)
                        .fetch();
```

- 타입을 명확하게 지정할 수 있다.



##### 튜플 조회

프로젝션 대상이 둘 이상일 때 사용

```java
List<Tuple> result = queryFactory
                        .select(member.username, member.age)
                        .from(member)
                        .fetch();

for (Tuple tuple : result) {
    String username = tuple.get(member.username);
    Integer age = tuple.get(member.age);
}
```

프로젝션 대상이 둘 이상일 때 각각 타입이 다를 수도 있다. querydsl은 이 문제를 Tuple을 사용해서 해결했다.



##### DTO 조회

- 프로퍼티 접근
- 필드 직접 접근
- 생성자 사용



###### 프로퍼티 접근

```java
List<MemberDto> result = queryFactory
                            .select(Projections.bean(MemberDto.class,
                                                   member.username,
                                                   member.age))
                            .from(member)
                            .fetch();
```

MemberDto의 Setter를 통해 프로퍼티에 접근하여 값 주입



###### 필드 직접 접근

```java
List<MemberDto> result = queryFactory
                            .select(Projections.fields(MemberDto.class,
                                                      member.username,
                                                      member.age))
                            .from(member)
                            .fetch();
```

필드에 값을 바로 주입 -> 리플렉션을 사용해서 주입하는 듯?



###### 생성자 사용

```java
List<MemberDto> result = queryFactory
    .select(Projections.constructor(MemberDto.class,
                                   member.username,
                                   member.age))
    .from(member)
    .fetch();
```

클래스의 생성자를 사용해서 값 주입. 생성자 파라미터의 타입에 매칭한다.



##### @QueryProjection

DTO의 생성자에 `@QueryProjection`을 붙이면 DTO의 Qtype이 생성된다.

```java
@Data
public class MemberDto {
    //...
    
    @QueryProjection
    MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
```

```java
List<MemberDto> result = queryFactory
                            .select(new QMemberDto(member.username, member.age))
                            .from(member)
                            .fetch();
```

jpql의 new 오퍼레이션을 사용하듯이 생성자로 바로 값을 주입받을 수 있다. 하지만, 사용할 때 고려해봐야 할 점이 많다.

- DTO는 여러가지 계층을 돌아다닌다.
- 그 DTO가 Querydsl에 의존한다.
- 여러 계층에서 Querydsl에 의존할 수 있다.
- tuple로 조회해서 dto로 변환해서 반환하는 방식을 채택할 수 있다.



### 동적 쿼리

QueryDsl을 사용해서 동적쿼리를 작성하는 방법은 2가지가 있다.

- **BooleanBuilder**
- **Where 다중 파라미터 사용**



#### BooleanBuilder 사용

```java
@Test
public void dynamicQuery_BooleanBuilder() {
    
    String usernameParam = "member1";
	Integer ageParam = 10;
    
    List<Member> result = searchMember1(usernameParam, ageParam);
}

private List<Member> searchMember1(String usernameCond, Integer ageCond) {
    
    BooleanBuilder builder = new BooleanBuilder();
    if(usernameCond != null) {
        builder.and(member.username.eq(usernameCond));
    }
    
    if(ageCond != null) {
        builder.and(member.age.eq(ageCond));
    }
    
    return queryFactory
            .selectFrom(member)
            .where(builder)
            .fetch();
}
```

- BooleanBuilder에 검색 조건을 쌓아서 조회한다.



#### Where 다중 파라미터 사용

```java
@Test
public void dynamicQuery_BooleanBuilder() {
    
    String usernameParam = "member1";
	Integer ageParam = 10;
    
    List<Member> result = searchMember2(usernameParam, ageParam);
}

private List<Member> searchMember2(String usernameCond, Integer ageCond) {
    return queryFactory
            .selectFrom(member)
            .where(usernameEq(usernameCond), ageEq(ageCond))
            .fetch();
}

private BooleanExpression usernameEq(String usernameCond) {
    return usernameCond != null ? member.username.eq(usernameCond) : null;
}

private BooleanExpression ageEq(Integer ageCond) {
    return ageCond != null ? member.age.eq(ageCond) : null;
}
```

- 조건문을 함수로 빼서 관리하는 방법
- 조건 함수가 null 을 반환해서 where문에 null 이 들어가면 그 조건문은 무시된다.
- 코드가 BooleanBuilder를 사용하는 방법보다 명시적이고 간결하다.
- 재사용 가능하다.

- 조건문 조합도 가능하다.

```java
private BooleanExpression allEq(String usernameCond, Integer ageCond) {
    return usernameEq(usernameCond).and(ageEq(ageCond));
}
```



### 벌크 연산

```java
// 단순 수정
long count = queryFactory
    			.update(member)
    			.set(member.username, "비회원")
    			.where(member.age.lt(28))
    			.execute();

// 기존 숫자에 1더하기
long count = queryFactory
    			.update(member)
    			.set(member.age, member.age.add(1))
    			.execute();

// 쿼리 한번으로 대량 데이터 삭제
long count = queryFactory
    			.delete(member)
    			.where(member.age.gt(20))
    			.execute();
```

- 벌크 연산을 할 때 주의할 점은 영속성 컨텍스트와 DB와 동기화가 되지 않는 문제이다.
- 따라서,  벌크 연산을 수행한 후 `em.flush()`, `em.clear()`로 영속성 컨텍스트를 초기화 하는 것이 안전하다.



### SQL function 호출하기

SQL function은 JPA와 같이 Dialect에 등록된 내용만 호출할 수 있다.

```java
// replace 함수 사용 -> 멤버 이름의 "member"를 "M"으로 변경해서 조회
List<String> result = queryFactory
    	.select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})",
                                          member.username, "member", "M"))
    	.from(member)
    	.fetch();

// 멤버 이름을 소문자로 변경해서 비교
List<String> result = queryFactory
    	.select(member.username)
    	.from(member)
    	.where(member.username.eq(Expressions.stringTemplate("function('lower',{0})",
                                                            member.username)))
    	.fetch();

// lower과 같은 ansi 표준 함수들은 querydsl이 상당부분 내장하고 있다. 위의 결과와 동일하다.
List<String> result = queryFactory
    	.select(member.username)
    	.from(member)
    	.where(member.username.eq(member.username.lower()))
    	.fetch();
```



#### QueryDsl 장단점

- 장점 
  - 동적 쿼리를 작성하기 쉽다.
  - **JPQL과 달리 컴파일 시점에 오류를 발견할 수 있다.**
- 단점
  - 초기 설정이 복잡하다.
  - 더이상 개발이 진행되지 않고 있다.