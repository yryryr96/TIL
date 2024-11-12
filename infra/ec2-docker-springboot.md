### Elastic IP Address (탄력적 IP)

**동적 클라우드 컴퓨팅을 위해 고안된 정적 IPv4 주소**

기존 aws ec2 인스턴스는 종료했다가 다시 시작하면 ip가 유동적으로 변경됨.

탄력적 IP를 설정하면 ec2 인스턴스를 종료하고 다시 실행해도 ip가 변하지 않음.



### EC2 접속

개인키가 있는 폴더의 파워쉘에서 `ssh -i "test-keypair.pem" ec2-user@ec2-3-39-75-195.ap-northeast-2.compute.amazonaws.com` 명령어 실행



### 인스턴스에 있는 모든 패키지 업데이트

`sudo yum update -y`



### yum으로 Docker 설치

`sudo yum install docker -y`



### Docker



##### 기본 명령어

- `docker ps` : 실행되고 있는 컨테이너
- `docker ps -a` 실행되거나 멈춘 모든 컨테이너
- `docker images` : 도커에 설치된 이미지
- `docker stop <container ID>`: 컨테이너 중지
- `docker rm <container ID>` : 컨테이너 삭제
- `docker rmi <image ID>` : 이미지 삭제



##### Dockerfile 작성

```dockerfile
FROM bellsoft/liberica-openjdk-alpine:17
# or
# FROM openjdk:8-jdk-alpine
# FROM openjdk:11-jdk-alpine

CMD ["./gradlew", "clean", "build"]
# or Maven
# CMD ["./mvnw", "clean", "package"]
# RUN ./gradlew clean build

VOLUME /tmp

ARG JAR_FILE=build/libs/*.jar
# or Maven
# ARG JAR_FILE_PATH=target/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]
```

- CMD 부분을 RUN 으로 하면 도커 실행 과정에서 build
  - 아니면 직접 빌드해주고 실행



##### 컨테이너 이미지 생성

DockerHub키고 명령어 실행

`docker build -t <컨테이너 이미지 이름 .`



##### 컨테이너 실행

`docker run -d --name <컨테이너 이름> -p 8080:8080 <실행할 이미지 이름>`

- -p 8080:8080 은 포트포워딩 하는 명령어
  - 8080(앞)은 host 즉, docker가 설치된 환경의 포트 번호
  - 8080(뒤)는 컨테이너 내부에서 애플리케이션이 실행되는 포트 번호