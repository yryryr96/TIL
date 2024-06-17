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



#### QueryDsl 장단점

- 장점 
  - 동적 쿼리를 작성하기 쉽다.
  - **JPQL과 달리 컴파일 시점에 오류를 발견할 수 있다.**
- 단점
  - 초기 설정이 복잡하다.
  - 더이상 개발이 진행되지 않고 있다.