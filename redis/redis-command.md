# redis 명령어



##### **set [key] [value]** : key, value 등록

- set name "youngrok"
- set os "window"



##### get [key] : key 조회

- get name
- get os



##### del [key] : key 삭제

- del name



##### **keys *** : 모든 key 조회



##### flushall : 모든 key 삭제



##### set [key] [value] ex [time] : key, value 등록하면서 만료시간(expired) 설정 

- set hobby "computer" ex 10



##### ttl [key] : time to live -> 남은 시간 확인

- ttl hobby

- 0 이상 숫자 : 남은 시간
- -1 : 만료 시간이 설정되지 않은 key
- -2 : 만료돼서 없어진 key