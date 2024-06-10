### Page

JpaRepository 파라미터에 `Pageable` 을 전달하면 `Page` 객체를 반환 받을 수 있다. 이 Page 객체 내부에 page, total count 등 다양한 정보를 포함하고 있다.



##### 예시

```java
//repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    Page<Member> findByAge(int age, Pageable pageable);
}

//test
@Test
void page() throws Exception {
    
    //given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    //when
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC,"username"));
    Page<Member> page = memberRepository.findByAge(10, pageRequest);
    
    //then
    List<Member> content = page.getContent(); //조회된 데이터
    assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
    assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
    assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
    assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
    assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
    assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
}
```



### Slice

- Slice는 Page와 다르게 count 쿼리를 발생시키지 않고, `limit + 1`개의 데이터를 조회한다. 그래서 다음 페이지 여부를 확인할 수 있다.
  - limit+1을 조회해서 데이터가 있으면 다음 페이지가 존재 -> 더보기 표시
  - 없으면 끝.
  - 이런 식으로 무한 스크롤 구현 가능



### count query 분리

복잡한 join이 발생한 쿼리에서 데이터 개수를 조회하는 count 쿼리가 발생할 때 불필요한 join이 발생할 수 있다.

##### 

##### count query 분리 전

```java
@Query("select m from Member m left join m.team t")
Page<Member> findByAge(int age, Pageable pageable);
```

이와 같은 상황에서 데이터를 조회할 때 left outer join이 발생한다. 뿐만 아니라, Page 객체를 구현해야 하기 때문에 추가적으로 count query도 같이 발생한다. 이 때, count 쿼리에서도 left outer join이 발생한다.

실질적으로 위와 같은 쿼리에서는 count query에 join이 필요가 없다. (left join은 데이터 수에 변화가 없기 때문이다.)

따라서, 불필요한 join을 없애기 위해 count query를 분리하자.



##### count query 분리 후

```java
@Query(value = "select m from Member m left join m.team t",
      countQuery = "select count(m) from Member m")
Page<Member> findByAge(int age, Pageable pageable);
```

위와 같이 `@Query`의 `countQuery`에 count query를 작성해서 불필요한 join을 없앨 수 있다. -> 성능 최적화 가능



### 벌크 연산

##### 

##### 순수 JPA 벌크 연산

```java
// bulk 연산, 몇 개의 row가 영향을 받았는지 반환
public int bulkAgePlus(int age) {
    return em.createQuery("update Member m set m.age = m.age + 1 where m.age >= :age")
        .setParameter("age", age)
        .executeUpdate();
}

//test
@Test
public void bulkUpdate() throws Exception {
    //given
    memberJpaRepository.save(new Member("member1", 10));
    memberJpaRepository.save(new Member("member2", 19));
    memberJpaRepository.save(new Member("member3", 20));
    memberJpaRepository.save(new Member("member4", 21));
    memberJpaRepository.save(new Member("member5", 40));
    
    //when
    int resultCount = memberJpaRepository.bulkAgePlus(20);
    
    //then
    assertThat(resultCount).isEqualTo(3); 
}
```

위 코드에서 조심해야 할 부분이 있다. 벌크 연산을 수행한 후 영속성 컨텍스트에 있는 데이터와 DB에 있는 실제 데이터가 다를 수 있다.

벌크 연산은 우선 영속성 컨텍스트를 flush하고, DB에 직접적으로 쿼리를 수행하기 때문에 영속성 컨텍스트에는 벌크 연산 이전의 데이터들이 저장되어 있다.

이 문제를 해결하기 위해선 영속성 컨텍스트를 clear 해주면 된다. `em.clear()`



##### 스프링 데이터 JPA 벌크 연산

```java
// MemberRepository
@Modifying(clearAutomatically = true)
@Query("updtae Member m set m.age = m.age + 1 where m.age >= :age")
int bulkAgePlus(@Param("age") int age);
```

- `@Modifying` : **executeUpdate()**의 역할을 한다고 보면 된다. @Modifying이 없으면 `getSingleResult()`, `getResultList()` 로 동작한다. 
- `clearAutomatically` : 메서드 실행 후 영속성 컨텍스트를 초기화하는 옵션이다.



### @EntityGraph

- 연관된 엔티티들을 SQL 한번에 조회하는 방법
- Spring Data Jpa는 fetch join을 사용하기 위해서 직접 쿼리를 작성해야 한다. 이를 간편화 하기 위해 `@EntityGraph`를 사용한다.



##### EntityGraph

```java
public interface MemberRepository extends JpaRepository<Member,Long> {
    
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();
    
    @Entitygraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findAllEntityGraph();
}
```

- `@EntityGraph`의 attributePaths에 작성된 필드와 fetch join 한다.
- 즉 attributePaths에 작성된 필드를 모두 즉시 로딩해서 가져온다.



##### NamedEntityGraph

```java
@NamedEntityGraph(name = "Member.all", attributeNodes = @NamedAttributeNode("team"))
@Entity
public class Member {}
```

```java
// repository
@EntityGraph("Member.all")
@Query("select m from Member m")
List<Member> findMemberEntityGraph();
```

