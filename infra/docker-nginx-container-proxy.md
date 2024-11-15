### docker-nginx-container-proxy



### 목표

container로 띄워놓은 server1, server2에 nginx로 reverse proxy 적용

- `server1` : 8080 port
- `server2` : 8081 port

- public ip:80 접속 -> server1로 포트포워딩
- public ip:81 접속 -> server2로 포트포워딩



##### docker-compose.yml

```dockerfile
version: "3.9"

networks:
  app-network:
    driver: bridge

services:

  web:
    image: nginx
    restart: always
    ports:
      - 80:80
      - 81:81
      - 443:443
    depends_on:
      - server1
      - server2
    volumes:
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
    networks:
      - app-network
  
  server1:
    build:
      context: ./server1
      dockerfile: Dockerfile

    container_name: server1
    ports:
      - 8080:8080
    networks:
          - app-network
  server2:
    build:
      context: ./server2
      dockerfile: Dockerfile

    container_name: server2
    ports:
      - 8081:8080
    networks:
      - app-network
```

- ec2 환경에 nginx 이미지를 pull 받아 놓은 상테에서 테스트 진행
- `services.web.image` : 컨테이너에 올릴 image 명 (이미 nginx 이미지가 있는 상태)
- `services.web.ports` : nginx 포트포워딩 (host port : docker port)

- `services.web.depends_on` : server1, server2에 의존해서 생성
- `services.web.volumes` : 경로A : 경로B 형태를 띄는데 docker-compose.yml의 상대경로 경로A의 파일을 nginx 컨테이너 안의 경로B에 마운트
  - **nginx 설정 파일을 작성해서 nginx 컨테이너 안에 설정을 추가하는 것**



##### default.conf

```dockerfile
# 백엔드 upstream 설정
upstream server1-app {
    server server1:8080;
}

upstream server2-app {
    server server2:8080;
}

server {
    listen 80;

    location / {
        proxy_pass http://server1-app/;
    }

    location /health {
        proxy_pass http://server1-app/health;
    }
}

server {
    listen 81;

    location / {
        proxy_pass http://server2-app/;
    }

    location /health {
        proxy_pass http://server2-app/health;
    }
}
```

- `upstream`
  - **server 서비스명:포트** : nginx와 docker-compose.yml의 `services에 작성한 서비스명`:`포트`를 여는 것
  - 위 설정에선 nginx가 server1:8080, server2:8080 포트를 열어서 통신할 수 있음.

- `server`
  - `listen` : 수신 포트
  - `location` : 경로
    - `proxy_pass` : location 경로로 들어왔을 때 포트포워딩 할 ip
    - location 뒤 경로는 여러 방법으로 작성할 수 있음.
      - 정확히 일치
      - 정규식 등



### 결과

따라서 nginx로 reverse proxy를 적용해서 `public_ip:80/health -> server1:8080/health`, `public_ip:81/health -> server2:8080/health` 를 만족했다.



### 회고

server1, server2 컨테이너가 docker에 올라가고 nginx 포트포워딩 하는 시간이 추가적으로 필요한 것 같다. docker-compose를 실행하고 8080, 8081 포트로 접속하면 바로 서버에 접속이 되지만 80, 81 포트로 접속하면 바로 접속이 안되고 시간이 좀 지나고 된다. **(여기서 포트포워딩 안되는 줄 알고 계속 다시 해봄)**

`docker exec -it <container id> /bin/bash`로 container에 접속해서 log를 확인하던가 `docker logs <container id>`로 로그 확인

`docker ps -a` 명령어로 nginx의 status를 확인하자. -> 계속 Restarting 하는 경우가 있었음



###  더 알아볼 것

- location 작성 규칙
- nginx.conf와 default.conf 차이