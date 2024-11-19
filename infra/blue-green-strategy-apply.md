# Blue / Green 무중단 배포 적용기

배포 자동화를 통해 애플리케이션을 배포했을 때 `downtime`이 약 30초 정도 발생했다. 

downtime을 없애기 위해 무중단 배포를 해야겠다 생각했고, 트래픽을 조절하는게 복잡할 것이라 생각하고 구버전, 신버전이 함께 존재하는 것이 싫었기 때문에 Blue / Green 배포 방식을 채택했다. 

기존에 nginx를 사용하고 있었고, 한 개의 EC2 안에 `8080`, `8081` 포트를 `Blue`, `Green` 컨테이너로 설정해서 무중단 배포를 적용했다.



##### 클라이언트 접근 흐름

1.  클라이언트가 브라우저로 내 도메인에 접근한다 `443 포트`
2. nginx에서 포트포워딩한다. `80` -> `443`, `443` -> `8080`, `8081`



##### 무중단 배포 흐름

1. 구버전 `server-a`가 `8080`포트에 연결되어 있는 상태이다.
2. 신버전 `server-b`가 github repository에 push 되면 github actions의 workflow가 작동한다.
3. `deploy.sh`를 실행시켜 `blue/green` 스크립트를 실행한다.
4. `8081` 포트에 `server-b`를 실행하고 `8080` 포트의 `server-a`를 중지한다.

위 흐름대로 작업이 진행되면 신버전이 EC2에 완전히 실행되고 구버전 애플리케이션이 중지되므로 `downtime`을 최소화할 수 있다.



기존에 nginx도 docker-compose로 container로 관리했는데 굳이 매번 배포할 때 마다 새로운 이미지를 받아올 필요가 없다고 생각해서 EC2 환경에 직접 설치했다.

```shell
sudo yum install nginx
```



##### service-url.inc

```
set $service_url http://127.0.0.1:8080;
```

`default.conf` , `deploy.sh`에서 `include`해서 사용할 `기본 ip`이다.



##### default.conf

```shell
server {

    include /etc/nginx/conf.d/service-url.inc;
    listen 443 ssl;
    server_name evan523.shop;

    ssl_certificate /etc/letsencrypt/live/evan523.shop/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/evan523.shop/privkey.pem;

    location / {
        proxy_pass $service_url;
        proxy_set_header X-Real-Ip $remote_addr;
        proxy_set_header x-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Host $host;
    }
}

server {
    listen 80;
    server_name evan523.shop;

    return 301 https://$host$request_uri;
}
```

- `443` 포트로 들어오는 모든 경로 요청은 `service-url.inc`에 정의한 `$service_url`로 `reverse proxy`
- `80` 포트로 들어오는 요청은 https 즉 `443` 포트로 포트포워딩



##### docker-compose.a.yml

```yaml
version: "3"

services:

  server-a:
    image: evan523/server1

    container_name: server-a
    ports:
      - 8080:8080

    restart: always

networks:
  default:
    external:
      name: evannet
```



##### docker-compose.b.yml

```yaml
version: "3"

services:

  server-b:
    image: evan523/server1

    container_name: server-b
    ports:
      - 8081:8080

    restart: always

networks:
  default:
    external:
      name: evannet
```

`docker-compose.a.yml`, `docker-compose.b.yml`을 만들어서 `blue / green`에 따라 실행해준다.

##### 

##### deploy.sh

```sh
RUNNING_CONTAINER=$(sudo docker ps -a)
echo "실행중인 컨테이너 목록: ${RUNNING_CONTAINER}"

# 실행중인 도커 컴포즈 확인
EXIST_A=$(sudo docker ps -q -f name=server-a)

echo "EXIST_A 값: ${EXIST_A}"

if [ -z "${EXIST_A}" ]
then
        # B가 실행중인 경우
        START_CONTAINER=a
        TERMINATE_CONTAINER=b
        START_PORT=8080
        TERMINATE_PORT=8081
else
        # A가 실행중인 경우
        START_CONTAINER=b
        TERMINATE_CONTAINER=a
        START_PORT=8081
        TERMINATE_PORT=8080

fi

echo "server-${START_CONTAINER} up"

sudo docker pull evan523/server1:latest
sudo docker-compose -f docker-compose.${START_CONTAINER}.yml up -d --build

RUNNING_CONTAINER=$(sudo docker ps -a)
echo "실행중인 컨테이너 목록: ${RUNNING_CONTAINER}"

for cnt in {1..10}
do
        echo "check server start..."

        UP=$(curl -s http://127.0.0.1:${START_PORT}/health | grep 'running')
        if [ -z "${UP}" ]
        then
                echo "server not start..."
        else
                break
        fi

        echo "wait 10 seconds"
        sleep 10
done

if [ $cnt -eq 10 ]
then
        echo "deployment failed."
        exit 1
fi

echo "server start!"
echo "change nginx server port"

# sed 명령어를 이용해서 아까 지정해줬던 service-url.inc의 url값을 변경해줍니다.
# sed -i "s/기존문자열/변경할문자열" 파일경로 입니다.
# 종료되는 포트를 새로 시작되는 포트로 값을 변경해줍니다.
sudo sed -i "s/${TERMINATE_PORT}/${START_PORT}/" /etc/nginx/conf.d/service-url.inc

# 새로운 포트로 스프링부트가 구동 되고, nginx의 포트를 변경해주었다면, nginx 재시작해줍니다.echo "nginx reload.."
sudo service nginx reload


# 기존에 실행 중이었던 docker-compose는 종료시켜줍니다.
echo "server-${TERMINATE_CONTAINER} down"
sudo docker-compose -f docker-compose.${TERMINATE_CONTAINER}.yml down
echo "success deployment"
```



- ```sh
  EXIST_A=$(sudo docker ps -q -f name=server-a)
  
  echo "EXIST_A 값: ${EXIST_A}"
  
  if [ -z "${EXIST_A}" ]
  then
          # EXIST_A 값이 공백인 경우 or B가 실행중인 경우
          START_CONTAINER=a
          TERMINATE_CONTAINER=b
          START_PORT=8080
          TERMINATE_PORT=8081
  else
          # EXIST_A 값이 공백이 아닌 경우 -> A가 실행중인 경우
          START_CONTAINER=b
          TERMINATE_CONTAINER=a
          START_PORT=8081
          TERMINATE_PORT=8080
  
  fi
  ```

  - `EXIST_A`에는 name이 server-a라는 컨테이너가 있으면 컨테이너의 ID가 들어가고 없으면 ""가 들어간다.
  - `if [ -z "${EXIST_A}" ]` : EXIST_A 값이 공백이면 `true`, 공백이 아니면 `false`
    - 그 아래 코드는 서버 실행 상태에 따라 변수 할당
    - `START_CONTAINER` : 이번에 실행할 컨테이너 값
    - `TERMINATE_CONTAINER` : 이번에 종료할 컨테이너 값
    - `START_PORT` : 이번에 애플리케이션을 실행할 포트
    - `TERMINATE_PORT` : 이번에 종료할 포트 (이전에 애플리케이션을 실행하고 있던 포트)

- ```sh
  sudo docker pull evan523/server1:latest
  sudo docker-compose -f docker-compose.${START_CONTAINER}.yml up -d --build
  ```

  - Github Actions에서 push 한 최신 버전의 애플리케이션 image를 pull 받는다.
  - 그리고 이번에 실행할 `docker-compose.${START_CONTAINER}.yml`을 빌드하고 실행

- ```sh
  for cnt in {1..10}
  do
          echo "check server start..."
  
          UP=$(curl -s http://127.0.0.1:${START_PORT}/health | grep 'running')
          if [ -z "${UP}" ]
          then
                  echo "server not start..."
          else
                  break
          fi
  
          echo "wait 10 seconds"
          sleep 10
  done
  
  if [ $cnt -eq 10 ]
  then
          echo "deployment failed."
          exit 1
  fi
  ```

  - 이번에 애플리케이션을 실행할 포트로 요청을 보내서 health check
    - `UP=$(curl -s http://127.0.0.1:${START_PORT}/health | grep 'running')` : 해당 경로로 요청을 보내고 반환되는 값에 "running" 이 포함되어 있는지 확인
  - `if [ -z "${UP}" ]` : 비어있다면 다시 시도, 반환 값이 옳다면 break
  - `if [ $cnt -eq 10 ]` : cnt 값이 10 이면 실패

- ```sh
  # sed 명령어를 이용해서 아까 지정해줬던 service-url.inc의 url값을 변경해줍니다.
  # sed -i "s/기존문자열/변경할문자열" 파일경로 입니다.
  # 종료되는 포트를 새로 시작되는 포트로 값을 변경해줍니다.
  sudo sed -i "s/${TERMINATE_PORT}/${START_PORT}/" /etc/nginx/conf.d/service-url.inc
  
  # 새로운 포트로 스프링부트가 구동 되고, nginx의 포트를 변경해주었다면, nginx 재시작해줍니다.echo "nginx reload.."
  sudo service nginx reload
  
  # 기존에 실행 중이었던 docker-compose는 종료시켜줍니다.
  echo "server-${TERMINATE_CONTAINER} down"
  sudo docker-compose -f docker-compose.${TERMINATE_CONTAINER}.yml down
  echo "success deployment"
  ```

  - `service-url.inc`에 정의한 $service_url 변경
    - 포트포워딩 하는데 사용하는 값이니 애플리케이션이 실행되는 포트로 변경해줘야 한다.
    - 변경하고 nginx를 다시 시작해줘야 한다.
  - 기존에 실행되던 docker-compose를 중지한다.



### 트러블 슈팅



##### blue/green port 할당 문제

docker-compose.yml에서 server-a, server-b 에 따른 포트를 설정해줬었다.

ec2 환경에서 localhost로 container network에 접근하기 위해 `    network_mode: host	` 라는 코드를 작성했었는데 이 코드는 container에서 사용하는 network와 host network를 동일시 여긴다는 뜻이었다. 

`docker-compose.b.yml`에서 `ports 8081:8080`으로 포트포워딩을 했는데 host의 8080포트에는 server-a가 이미 할당되어 있었기 때문에 정상적인 포트포워딩이 이루이지지 않아 배포 과정에서 오류가 발생했다.

따라서, EC2 환경에 `docker network create <network_name>`으로 네트워크를 생성해주고

```yaml
networks:
  default:
    external:
      name: <network_name>
```

과 같이 네트워크를 할당해서 컨테이너에 접근했다.



##### Spring 애플리케이션 Image 업데이트 문제

새 버전 push 시에 포트가 변경되고 컨테이너가 새로 생성되어 배포되는 것 까지 정상적으로 작동했다.

하지만, 이미지가 최신 버전으로 업데이트 되지 않아 신버전을 배포했지만 내용은 구버전 그대로인 문제가 발생했다.

도커 image가 pull 되지 않는 것 같아 Github Actions의 workflow에 `sudo docker pull ${{ secrets.DOCKER_USERNAME }}/${{ matrix.service }}:latest` 코드를 추가해서 명시적으로 image를 pull 해봤지만 문제는 해결되지 않았다.

직접 EC2에 접속해서 image를 pull 하면 정상적으로 image가 업데이트 되는 것을 확인하고 docker login이나 권한에 문제가 있을 것이라고 생각했다.

**결과적으로 `deploy.sh` 에서 명시적으로 도커 image의 최신 버전을 pull 받아서 업데이트 해주는 방법으로 해결했다.**



### 회고

`배포 자동화`가 개발자를 위한 구현이었다면 `무중단 배포`는 사용자를 위한 기능이라고 생각한다. (downtime을 줄여 서비스를 원활하게 이용)

구현이 간단할 줄 알았는데 막상 구현하다 보니 다양한 에러가 발생해서 시간이 오래 걸렸다..

`docker-compose.yml`, `deploy.sh` 에 docker_username이랑 image명 등 중요 정보를 직접 입력하는 방법은 좋지 않은 것 같다.

- EC2 전역 변수로 값을 할당해서 사용
- Github Actions의 Secrets에 값을 할당하고 파일을 생성해서 해당 경로로 복사하는 방법

위 두 가지 방법 중 하나를 선택해서 보안을 강화하는 것이 좋을 것 같다. 차후에 적용..



Github Actions를 사용해서 배포 자동화부터 무중단 배포까지 구현해보았는데 한 번 구현해놓기만 업무 효율이 매우 증가할 것 같다.. 있다가 없으면 매우 체감이 클 듯하다.