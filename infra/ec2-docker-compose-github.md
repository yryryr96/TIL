### server1, server2를 docker container로 만들고 docker-compose로 관리하게 하고싶음

- project 폴더에 docker-compose.yml, server1, server2가 위치
- 이 project를 github repository로 관리
- ec2에서 repository를 clone해서 docker-compose 사용

- ec2의 8080 포트는 server1, 8081 포트는 server2로 라우팅



#### Docker-compose

여러 개의 Docker Container를 하나의 서비스로 정의하고 구성해 하나의 묶음으로 관리할 수 있는 기술



`폴더구조`

project

- server1
  - Dockerfile
- server2
  - Dockerfile
- docker-compose.yml

```dockerfile
version: "3.9"
services:
  server1:
    build:
      context: ./server1
      dockerfile: Dockerfile

    container_name: server1
    ports:
      - 8080:8080

  server2:
    build:
      context: ./server2
      dockerfile: Dockerfile

    container_name: server2
    ports:
      - 8081:8080
```



### ec2 프리티어에서 빌드하려고 하니 메모리 부족때문에 서버가 터짐

`t2.micro 스펙`

- 1 vCPU
- 1 GiB 메모리

SpringBootApplication을 빌드하는 데 많은 메모리를 사용 -> 1GiB 메모리로 부족

- 메모리 용량을 증가시키는 scale-up을 통해 문제를 해결할 수 있음 -> 추가 비용 발생
- **리눅스 가상 메모리 활용 (Swapping)**
  1. dd 명령을 사용해 루트 파일 시스템에 스왑 파일을 생성. `bs`는 **블록 크기**, `count`는 **블록 수**
     - `$ sudo dd if=/dev/zero of=/swapfile bs=128M count=32`
     - 스왑 파일 크기 = 128M * 32 = 4GB
  2. 스왑 파일의 읽기 및 쓰기 권한을 업데이트
     - `$ sudo chmod 600 /swapfile`
  3. Linux 스왑 영역 설정
     - `$ sudo mkswap /swapfile`
  4. 스왑 공간에 스왑 파일을 추가해 스왑 파일을 즉시 사용할 수 있도록 함
     - `$ sudo swapon /swapfile`
  5. 절차가 성공적으로 완료되었는지 확인
     - `$ sudo swapon -s`
  6. 부팅시 /etc/fstab 파일을 편집하여 스왑 파일을 시작
     - `$ sudo vi /etc/fstab` : 편집기 작동
     - `/swapfile swap swap defaults 0 0` : 편집기 끝에 작성하고 저장



#### 문제

ec2에서 `docker-compose up --build -d` 명령어를 실행하면 server1, server2 Dockerfile에서 `COPY ${JAR_FILE} app.jar` 명령어를 실행하지 못함.

`build/libs/*` 경로를 찾지 못함. 즉, server1,2 가 build가 되지 않는 것 같다. 

Dockerfile에 RUN, CMD 등 여러 명령어를 사용해서 이미지가 생성될 때 build 되게 해보려 했지만, build 되는 속도보다 COPY 명령어가 실행되는 속도가 빨라서 경로를 찾지 못하는 것 같음.



#### 일시적 해결 방법

server1, server2를 local에서 빌드하고 gitignore에서 build/를 주석 처리 -> github repository에 server1,2의 build 파일이 올라가도록 수정

ec2에서 git clone으로 build 파일을 가져옴 -> `docker-compose up --build -d` 명령어 사용 시 server1,2의 Dockerfile을 읽고 경로 찾아서 문제 해결



문제는 해결했지만 local에서 직접 build하고 github repository에 올려야 한다는 점이 불편하다.

gitignore 수정한 것도 불편



#### 더 알아볼 것

- ec2 메모리 사용량을 늘린 만큼 Dockerfile을 읽으면서 build할 수 있는 방법 찾아보기
- 이 모든 과정을 자동화할 수 있는 방법 찾아보기 (github action, jenkins 등)