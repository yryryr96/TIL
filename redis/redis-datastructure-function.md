### edis 데이터베이스는 16 개로 구성되어 있다. 0 ~ 15

select [index]  (ex) select 0, select 10



### 데이터베이스 내 모든 키 조회

keys *



### 일반적인 String구조



#### set을 통해 key:value 세팅

`set user:email:1 test1@test.com`

`nx : 이미 존재하면 pass, 없으면 set `

`set user:email:2 test2@test.com nx`



#### ex(expiration) : 만료시간 (초단위), ttl(time to live)

`set user:email:3 test3@test.com ex 10`



#### redis 활용 : 사용자 인증정보 저장 (ex-refresh token)

`set user:1:refresh_token asdklflasdkf ex 100000`



#### 특정 key 삭제

`del user:email:1`



#### 현재 db내 모든 데이터 삭제

`flushdb`



#### redis 활용 1: 좋아요 기능 구현

##### rdb에서 관리할 시 동시성 이슈 발생 가능성이 높다.
`set likes:posting:1 0`
`incr likes:posting:1` # 특정 key 값의 value를 1만큼 증가
`decr likes:posting:1` # 특정 key 값의 value를 1만큼 감소
`get likes:posting:1`



#### redis 활용 2: 재고관리 기능 구현

`set stocks:product:1 100`
`decr stocks:product:1`
`get stocks:product:1`



#### redis 활용 3: 캐싱 기능 구현

`set posting:1 "{\"title\":\"hello java\", \"contents\":\"hello java is...\", \"author_email\":\"test1@test.com\"}" ex 100`



### list 자료구조: redis의 list는 deque 구조

`lpush` : 데이터를 왼쪽 끝에 삽입

`rpush` : 데이터를 오른쪽 끝에 삽입

`lpop`: 데이터를 왼쪽에서 꺼내기

`rpop`: 데이터를 오른쪽으로 꺼내기

lpush hongildongs hong1
lpush hongildongs hong2
rpush hongildongs hong3
rpop hongildongs
lpop hongildongs



#### list 조회

##### -1은 리스트 끝자리(마지막 index), -2는 끝에서 2번째

`lrange hongildongs 0 0` #첫번쨰 값 조회
`lragne hongildongs -1 -1` #마지막값만 조회
`lrange hongildongs 0 -1` #처음부터 끝까지
`lrange hongildongs -2 -1` #마지막 두번쨰부터 마지막자리까지
`lragne hongildongs 0 1` #처음부터 2번째까지



#### 데이터 개수 조회

`llen hongildongs`

#### ttl 적용

`expire hongildongs 10`

#### ttl 조회

`ttl hongildongs`

#### redis list 활용

`rpush mypages www.naver.com`
`rpush mypages www.google.com`
`rpush mypages www.daum.net`
`rpush mypages www.chatgpt.com`
`rpush mypages www.daum.com`



#### 최근 방문한 페이지 3개만 보여주는

`lrange mypages -3 -1`



### set 자료구조: 중복이 없고, 순서는 보장하지 않는 자료구조

#### set 값 추가

`sadd memberlist member1`
`sadd memberlist member1`
`sadd memberlist member2`



#### set 조회

`smembers memberlist`



#### set 요소 개수 조회

`scard memberlist`



#### set 멤버 제거

`srem memberlist member2`



#### 특정 요소가 set안에 들어있는지 확인

`sismember member1`



#### redis set 활용: 좋아요 구현

`sadd likes:posting:1 member1`
`sadd likes:posting:1 member2`
`sadd likes:posting:1 member1`



#### 좋아요 개수 구하기

`scard likes:posting:1`



#### 좋아요 눌렀는지 안눌렀는지 확인

`sismember likes:posting:1 member1`



### zset: sorted set

#### add하는 시점에 score를 부여하고, score를 기준으로 정렬

`zadd memberlist 3 member1`
`zadd memberlist 4 member2`
`zadd memberlist 1 member3`
`zadd memberlist 2 member4`



#### zset 조회: 기본적으로 score 기준 오름차순

`zrange memberlist 0 -1`



#### 내림차순 정렬

`zrevrange memberlist 0 -1`



#### score도 같이 조회

`zrange memberlist 0 -1 withscores`
`zrevrange memberlist 0 -1 withscores`



#### zset 요소 삭제

`zrem memberlist member4`



#### zrank: 특정 멤버가 몇번째 순서인지 출력(score 오름차순 기준)

`zrank memberlist member1`



#### redis zset 활용: 최근 본 상품목록

##### zset을 활용해 최근 시간순으로 score를 설정하여 정렬 (시간을 score로 변환하는 함수 사용)

`zadd recent:products 151930 pineapple`
`zadd recent:products 152030 banana`
`zadd recent:products 152130 orange`
`zadd recent:products 152230 apple`

##### zset도 set이므로 같은 상품을 add한 경우, 시간만 업데이트 되고 중복 제거 -> 덮어쓰기

`zadd recent:products 152330 apple`

##### 최근 본 상품 목록 3개 조회

`zrevrange recent:products 0 2 withscores`



### hashes: value값이 map 형태인 자료구조 key: <key, value>

#### 값 세팅, 여러 값 할당(redis 4.0 버전 이상 지원)
`hset member:info:1 name hong email hong@naver.com age 30`



#### 특정요소 조회

`hget member:info:1 name`



#### 모든 요소 조회

`hgetall member:info:1 -> name, email, age`



#### 특정 요소만 수정

`hset member:info:1 name kim`

#### 특정 요소의 값을 증가/감소 시킬 경우

`hincrby member:info:1 age 3`
`hincrby member:info:1 age -3`



#### redis hash 활용 예시: 빈번하게 변경되는 객체 값 캐싱

##### json형태의 문자열로 캐싱할 경우, 수정시에 문자열을 파싱하고 변경해야한다.



### redis pub/sub 실습

##### pub/sub 기능은 멀티 서버 환경에서 채팅, 알림 등의 서비스를 구현할 때 많이 사용
##### `server1 -> redis <- server2`


#### 터미널 2,3 실행

`subscribe test_channel`



#### 터미널 1 실행
`publish test_channel "hello, this is a test message"`



test_channel을 구독한 터미널 2,3에서 터미널1이 발행한 메시지를 확인할 수 있다.



### redis streams 실습: 데이터 실시간으로 read, 데이터가 저장
##### kafka와 비슷한 자료구조


##### * : ID 값 자동 생성

`xadd test_stream * message "hello, this is a stream message"`



##### $ : 현재 마지막 메세지 이후에 오는 새 메시지를 의미

`xread block 20000 streams test_stream $`



##### - + : 전체 메시지 조회

`xrange test_stream - +`